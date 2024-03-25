package com.example.mooditudeapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.app.Service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Binder;
import android.os.IBinder;

import android.util.Log;

import android.Manifest;

import androidx.core.app.NotificationCompat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import javax.annotation.Nullable;

public class SensorService extends Service {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private final IBinder binder = new LocalBinder();
    private int thresholdExceededCount = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        initializeBluetooth();
        startForeground(1, buildForegroundNotification());
        startScanningForHeartRateSensor();
    }

    private void initializeBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    private void startScanningForHeartRateSensor() {
        Log.i("SensorService", "startScanningForHeartRateSensor called.");

        //Check if Bluetooth permissions are granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {   // for versions over android 12
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.w("SensorService: startScanningForHeartRateSensor", "Bluetooth permissions not granted. Stopping Service...");
                stopForeground(true);
                stopSelf();
                return;
            }
        } else {            // for versions under android 12
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                Log.w("SensorService: startScanningForHeartRateSensor", "Bluetooth permissions not granted. Stopping Service...");
                stopForeground(true);
                stopSelf();
                return;
            }
        }

        //Check if Bluetooth is supported and enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.w("SensorService", "Bluetooth is not enabled.");
            return;
        }

        // Callback for found devices
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.i("SensorService", "onScanResult called.");
                BluetoothDevice device = result.getDevice();

                //Check if Bluetooth permissions are granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {   // for versions over android 12
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.w("SensorService: onScanResult", "Bluetooth permissions not granted.");
                        return;
                    }
                } else {            // for versions under android 12
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                        Log.w("SensorService: onScanResult", "Bluetooth permissions not granted.");
                        return;
                    }
                }

                //stop scanning and connect
                if ("Nano33BLE".equals(device.getName())) {
                    Log.d("SensorService", "Found device: " + device.getName());
                    bluetoothAdapter.getBluetoothLeScanner().stopScan(this);
                    connectToDevice(device);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e("SensorService", "BLE scan failed with code " + errorCode);
            }
        };

        // Start scanning
        bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("SensorService", "Connected to GATT server.");

                // Check for BLUETOOTH permissions

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {   // for versions over android 12
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        // Attempt to discover services after successful connection.
                        Log.i("SensorService: onConnectionStateChange", "Attempting to start service discovery:" + gatt.discoverServices());
                    } else {
                        Log.w("SensorService: onConnectionStateChange", "Lacking permissions.");
                    }
                } else {            // for versions under android 12
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                            checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                        // Attempt to discover services after successful connection.
                        Log.i("SensorService: onConnectionStateChange", "Attempting to start service discovery:" + gatt.discoverServices());
                    } else {
                        Log.w("SensorService: onConnectionStateChange", "Lacking permissions.");
                    }
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("SensorService", "Disconnected from GATT server.");
                if (gatt == bluetoothGatt) {
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                    startScanningForHeartRateSensor();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("SensorService", "GATT services discovered.");

                String serviceUUIDString = "19B10000-E8F2-537E-4F6C-D104768A1214";
                UUID serviceUUID = UUID.fromString(serviceUUIDString);
                UUID cccdUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");  // Standard UUID for the Client Characteristic Configuration Descriptor (CCCD)

                BluetoothGattService heartRateService = gatt.getService(serviceUUID);

                if (heartRateService != null) {
                    String characteristicUUIDSting = "19B10001-E8F2-537E-4F6C-D104768A1214";
                    UUID characteristicUUID = UUID.fromString(characteristicUUIDSting);
                    BluetoothGattCharacteristic heartRateCharacteristic = heartRateService.getCharacteristic(characteristicUUID);

                    // Verify if permissions have been granted
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {   // for versions over android 12
                        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Log.w("SensorService: onScanResult", "Bluetooth permissions not granted.");
                            return;
                        }
                    } else {            // for versions under android 12
                        if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                            Log.w("SensorService: onScanResult", "Bluetooth permissions not granted.");
                            return;
                        }
                    }

                    if (heartRateCharacteristic != null) {

                        // Characteristic notification setup to continuously receive heart rate data.
                        gatt.setCharacteristicNotification(heartRateCharacteristic, true);

                        // Find and write to the characteristic's CCCD to enable notifications
                        BluetoothGattDescriptor descriptor = heartRateCharacteristic.getDescriptor(cccdUUID);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            boolean success = gatt.writeDescriptor(descriptor);
                            if (success) {
                                Log.i("SensorService", "Successfully wrote to descriptor to enable notifications.");
                            } else {
                                Log.e("SensorService", "Failed to write to descriptor to enable notifications.");
                            }
                        } else {
                            Log.w("SensorService", "CCCD not found for the characteristic.");
                        }
                    } else {
                        Log.w("SensorService", "Heart Rate Characteristic not found");
                    }
                } else {
                    Log.w("SensorService", "Heart Rate Service not found");
                }
            } else {
                Log.w("SensorService", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i("SensorService", "onCharacteristicChanged is called.");

            String characteristicUUIDSting = "19B10001-E8F2-537E-4F6C-D104768A1214";
            UUID characteristicUUID = UUID.fromString(characteristicUUIDSting);



            if (characteristicUUID.equals(characteristic.getUuid())) {
                int hr = bytesToInt(characteristic.getValue());
                boolean eventDetected = eventDetectionAlgorithm(hr);
                if(heartRateCallback != null) {
                    heartRateCallback.onHeartRateUpdate(hr);
                }
            }
        }

        private int bytesToInt(byte[] bytes) {
            // Ensure the byte array has at least 4 bytes for an integer
            if (bytes != null && bytes.length >= 4) {
                return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
            }
            return 0; // Default value or error value
        }
    };

    private void connectToDevice(BluetoothDevice device) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {   // for versions over android 12
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.w("SensorService: connectToDevice", "Bluetooth permissions not granted.");
                return;
            }
        } else {            // for versions under android 12
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                Log.w("SensorService: connectToDevice", "Bluetooth permissions not granted.");
                return;
            }
        }

        //Close previous connection if exists
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }

        // Connect to the device. Returns a BluetoothGatt instance
        // used to interact with the device's GATT services.
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        Log.d("SensorService", "Trying to create a new connection.");
    }

    // Notification that tells user data collection of heart rate is running in the background
    private Notification buildForegroundNotification() {
        String channelId = createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                //.setSmallIcon(R.drawable.ic_heartbeat)
                .setContentTitle("Heart Rate Service")
                .setContentText("Collecting heart rate data in the background")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }

    private String createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "SensorServiceChannel";
            String channelName = "Heart Rate Sensor Service";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            return channelId;
        }
        return "";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {   // for versions over android 12
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.w("SensorService: onDestroy", "Permissions not granted.");
                return;
            }
        } else {            // for versions under android 12
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                Log.w("SensorService: onDestroy", "Permissions not granted.");
                return;
            }
        }

        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }

    public interface HeartRateCallback {
        void onHeartRateUpdate(int heartRate);
    }

    private HeartRateCallback heartRateCallback = null;
    public void registerHeartRateCallback(HeartRateCallback callback) {
        this.heartRateCallback = callback;
    }
    public void unregisterHeartRateCallback() {
        this.heartRateCallback = null;
    }

    private boolean eventDetectionAlgorithm(int hr) {

        if (hr >= 120) {
            thresholdExceededCount++;
            if (thresholdExceededCount > 5) {
                Log.i("eventDetectionAlgorithm", "Event detected.");
                return true;
            }
        } else {
            thresholdExceededCount = 0;
        }

        return false;
    }
}