package com.example.mooditudeapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import android.Manifest;

import androidx.core.app.NotificationCompat;

import javax.annotation.Nullable;

public class SensorService extends Service {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

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

        //Check if Bluetooth permissions are granted
        if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.w("SensorService", "Bluetooth permissions not granted.");
            return;
        }
        //Check if Bluetooth is supported and enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.w("SensorService", "Bluetooth is not enabled.");
            return;
        }

        // Define a callback for found devices
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                //Check if Bluetooth permissions are granted
                if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.w("SensorService", "Bluetooth permissions not granted.");
                    return;
                }
                Log.d("SensorService", "Found device: " + device.getName());
                //stop scanning and connect
                bluetoothAdapter.getBluetoothLeScanner().stopScan(this);
                connectToDevice(device);
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

                // Check for BLUETOOTH_CONNECT permission
                if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Attempt to discover services after successful connection.
                    Log.i("SensorService", "Attempting to start service discovery:" + gatt.discoverServices());
                } else {
                    Log.w("SensorService", "Lacking BLUETOOTH_CONNECT permission.");
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("SensorService", "Disconnected from GATT server.");
                if (gatt == bluetoothGatt) {
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("SensorService", "GATT services discovered.");
                // Here you can work with services and characteristics
                // For example, subscribe to the heart rate measurement notification
            } else {
                Log.w("SensorService", "onServicesDiscovered received: " + status);
            }
        }

        // Implement other callback methods like onCharacteristicRead, onCharacteristicChanged, etc.
    };

    private void connectToDevice(BluetoothDevice device) {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.w("SensorService", "BLUETOOTH_CONNECT permission not granted.");
            return;
        }

        //Close previous connection if exists
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }

        // Connect to the device. This method returns a BluetoothGatt instance
        // that you use to interact with the device's GATT services.
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

        if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.w("SensorService", "BLUETOOTH_CONNECT permission not granted.");
            return;
        }

        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
