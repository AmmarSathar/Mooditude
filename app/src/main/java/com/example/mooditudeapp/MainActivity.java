package com.example.mooditudeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.Manifest;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorService.HeartRateCallback {

    SensorService sensorService;
    boolean isBound = false;
    private TextView heartRateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        heartRateTextView = findViewById(R.id.HeartRateTextView);
        requestNecessaryPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, SensorService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_CODE = 2;
    private void requestNecessaryPermissions() {
        List<String> requiredPermissions = new ArrayList<>();   //Required permissions depending on SDK version

        // Permission for body sensors
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.BODY_SENSORS);
        }

        // Permission for coarse and fine location
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Bluetooth permissions for versions over android 12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {    // Bluetooth permissions for versions under android 12
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
        }

        Log.i("MainActivity", "requiredPermissions length: " + requiredPermissions.size());
        if (!requiredPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            startSensorService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            Log.i("MainActivity: 1", "grantResults length: " + grantResults.length);
            for (int grantResult : grantResults) {
                Log.i("MainActivity: 1", "grantResult: " + grantResult);
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                Log.i("MainActivity: 1", "allPermissionsGranted: true");
                // All foreground permissions have been granted, request background permissions (if applicable)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestBackgroundPermissions();
                } else {
                    Log.i("MainActivity: 1", "All permissions granted, starting Sensor Service...");
                    startSensorService();
                }
            } else {
                // Handles the case where the user denies the permission with UI (permission must be granted for app to be used)
                Log.w("MainActivity: 1", "Permissions not granted.");
            }
        } else if (requestCode == PERMISSION_REQUEST_BACKGROUND_CODE) {
            boolean backgroundPermissionsGranted = true;
            Log.i("MainActivity: 2", "grantResults length: " + grantResults.length);
            for (int grantResult : grantResults) {
                Log.i("MainActivity: 2", "grantResult: " + grantResult);
                if (grantResult != PackageManager.PERMISSION_GRANTED){
                    backgroundPermissionsGranted = false;
                    break;
                }
            }
            if (backgroundPermissionsGranted) {
                Log.i("MainActivity: 2", "backgroundPermissionsGranted: true");
                Log.i("MainActivity: 2", "All permissions granted, starting Sensor Service...");
                startSensorService();
            } else {
                Log.w("MainActivity: 2", "Permissions not granted.");
            }
        }
    }

    private void requestBackgroundPermissions() {
        List<String> requiredPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            requiredPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.BODY_SENSORS_BACKGROUND);
        }
        ActivityCompat.requestPermissions(this, requiredPermissions.toArray(new String[0]), PERMISSION_REQUEST_BACKGROUND_CODE);
    }

    private void startSensorService() {
        Intent serviceIntent = new Intent(this, SensorService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i("MainActivity", "MainActivity connected to SensorService.");
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            sensorService = binder.getService();
            isBound = true;

            sensorService.registerHeartRateCallback(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
            sensorService.unregisterHeartRateCallback();
        }
    };

    @Override
    public void onHeartRateUpdate(int heartRate){
        Log.i("MainActivity", "HEARTRATE: " + heartRate + " bpm");
        String text = heartRate + " bpm";

        runOnUiThread(() -> heartRateTextView.setText(text));
    }
}

