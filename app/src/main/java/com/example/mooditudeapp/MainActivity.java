package com.example.mooditudeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.Manifest;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestNecessaryPermissions();
    }

    private static final int PERMISSION_REQUEST_CODE = 1;
    private void requestNecessaryPermissions() {
        List<String> requiredPermissions = new ArrayList<>();   //Required permissions depending on SDK version
        requiredPermissions.add(Manifest.permission.BODY_SENSORS);

        // for versions over android 12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            requiredPermissions.add(Manifest.permission.BLUETOOTH);
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        // for versions under android 12
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
        }

        ActivityCompat.requestPermissions(this, requiredPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // All required permissions have been granted, start the sensor service
                startSensorService();
            } else {
                // Handle the case where the user denies the permission with UI (permission must be granted for app to be used)
            }
        }
    }

    private void startSensorService() {
        Intent serviceIntent = new Intent(this, SensorService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}

