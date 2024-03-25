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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mooditudeapp.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {   // implements SensorService.HeartRateCallback

//    SensorService sensorService;
//    boolean isBound = false;
//    private TextView heartRateTextView;

    private ActivityMainBinding binding;
    private boolean isExpanded = false;
    private Animation fromBottomFabAnim;
    private Animation toBottomFabAnim;
    private Animation rotateClockWiseFabAnim;
    private Animation rotateAntiClockWiseFabAnim;

    protected FloatingActionButton menuFab;
    protected FloatingActionButton calendarFab;
    protected FloatingActionButton heartRateFab;
    protected FloatingActionButton buttonFab;

    protected TextView calendarText;
    protected TextView heartRateText;
    protected TextView buttonText;

    private TextView moodText;
    private TextView moodValTextView;

    private ImageView joyImage;
    private ImageView sadImage;
    private ImageView anxietyImage;
    private ImageView confidenceImage;

    private Button joyfulButton;
    private Button sadButton;
    private Button anxiousButton;
    private Button confidentButton;

    private View joyBg;
    private View sadBg;
    private View anxiousBg;
    private View confidentBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        heartRateTextView = findViewById(R.id.HeartRateTextView);
        requestNecessaryPermissions();
        setupUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Intent intent = new Intent(this, SensorService.class);
//        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if(isBound) {
//            unbindService(connection);
//            isBound = false;
//        }
    }

    private void setupUI() {

        //defining the menu elements
        menuFab = findViewById(R.id.menuFab);
        calendarFab = findViewById(R.id.calendarFab);
        heartRateFab = findViewById(R.id.heartRateFab);
        buttonFab = findViewById(R.id.buttonFab);

        //defining the text of the menu elements
        calendarText = findViewById(R.id.calendarText);
        heartRateText = findViewById(R.id.heartRateText);
        buttonText = findViewById(R.id.buttonText);

        //defining the mood text and mood value
        moodText = findViewById(R.id.moodText);
        moodValTextView = findViewById(R.id.moodValTextView);

        //defining the mood buttons
        joyfulButton = findViewById(R.id.joyfulButton);
        sadButton = findViewById(R.id.sadButton);
        anxiousButton = findViewById(R.id.anxiousButton);
        confidentButton = findViewById(R.id.confidentButton);

        joyfulButton.setBackgroundColor(0xFF6C8352);

        //defining the mood images
        joyImage = findViewById(R.id.joyImage);
        sadImage = findViewById(R.id.sadImage);
        anxietyImage = findViewById(R.id.anxietyImage);
        confidenceImage = findViewById(R.id.confidenceImage);

        //Defining the background colors
        joyBg = findViewById(R.id.joyBg);
        sadBg = findViewById(R.id.sadBg);
        anxiousBg = findViewById(R.id.anxiousBg);
        confidentBg = findViewById(R.id.confidentBg);

        //"loading" the animations
        fromBottomFabAnim = AnimationUtils.loadAnimation(this, R.anim.from_bottom_fab);
        toBottomFabAnim = AnimationUtils.loadAnimation(this, R.anim.to_bottom_fab);
        rotateClockWiseFabAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_clock_wise);
        rotateAntiClockWiseFabAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anti_clock_wise);


        binding.menuFab.setOnClickListener(view -> {
            if (isExpanded) {
                shrinkFab();
            } else {
                expandFab();
            }
        });

        onHeartClick();
        onCalendarClick();
        onMoodClick();

        // Find the ImageButton by its id
        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to open the UserProfileActivity
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
            }
        });
    }

    private void shrinkFab() {
        binding.menuFab.startAnimation(rotateAntiClockWiseFabAnim);
        binding.calendarFab.startAnimation(toBottomFabAnim);
        binding.heartRateFab.startAnimation(toBottomFabAnim);
        binding.buttonFab.startAnimation(toBottomFabAnim);

        binding.calendarText.startAnimation(toBottomFabAnim);
        binding.heartRateText.startAnimation(toBottomFabAnim);
        binding.buttonText.startAnimation(toBottomFabAnim);

        isExpanded = !isExpanded;
    }

    private void expandFab() {
        binding.menuFab.startAnimation(rotateClockWiseFabAnim);
        binding.calendarFab.startAnimation(fromBottomFabAnim);
        binding.heartRateFab.startAnimation(fromBottomFabAnim);
        binding.buttonFab.startAnimation(fromBottomFabAnim);

        binding.calendarText.startAnimation(fromBottomFabAnim);
        binding.heartRateText.startAnimation(fromBottomFabAnim);
        binding.buttonText.startAnimation(fromBottomFabAnim);

        isExpanded = !isExpanded;
    }

    private void onHeartClick(){

        heartRateFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, HeartRateActivity.class));
            }
        });
    }

    private void onCalendarClick() {

        calendarFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, EventHistoryActivity.class));
            }
        });
    }

    private void onMoodClick() {

        joyfulButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Change background color
                joyBg.setVisibility(view.VISIBLE);
                sadBg.setVisibility(view.GONE);
                anxiousBg.setVisibility(view.GONE);
                confidentBg.setVisibility(view.GONE);

                //Make image visible
                joyImage.setVisibility(view.VISIBLE);
                sadImage.setVisibility(view.GONE);
                anxietyImage.setVisibility(view.GONE);
                confidenceImage.setVisibility(view.GONE);

                //Change mood text
                moodText.setText("Joy");

                //Change button color
                joyfulButton.setBackgroundColor(0xFF6C8352);
                sadButton.setBackgroundColor(0x9C27B0);
                anxiousButton.setBackgroundColor(0x9C27B0);
                confidentButton.setBackgroundColor(0x9C27B0);
            }
        });

        sadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sadBg.setVisibility(view.VISIBLE);
                joyBg.setVisibility(view.GONE);
                anxiousBg.setVisibility(view.GONE);
                confidentBg.setVisibility(view.GONE);

                sadImage.setVisibility(view.VISIBLE);
                joyImage.setVisibility(view.GONE);
                anxietyImage.setVisibility(view.GONE);
                confidenceImage.setVisibility(view.GONE);

                moodText.setText("Sadness");

                joyfulButton.setBackgroundColor(0x9C27B0);
                sadButton.setBackgroundColor(0xFF6C8352);
                anxiousButton.setBackgroundColor(0x9C27B0);
                confidentButton.setBackgroundColor(0x9C27B0);
            }
        });

        anxiousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anxiousBg.setVisibility(view.VISIBLE);
                sadBg.setVisibility(view.GONE);
                joyBg.setVisibility(view.GONE);
                confidentBg.setVisibility(view.GONE);

                anxietyImage.setVisibility(view.VISIBLE);
                joyImage.setVisibility(view.GONE);
                sadImage.setVisibility(view.GONE);
                confidenceImage.setVisibility(view.GONE);

                moodText.setText("Anxiety");

                joyfulButton.setBackgroundColor(0x9C27B0);
                sadButton.setBackgroundColor(0x9C27B0);
                anxiousButton.setBackgroundColor(0xFF6C8352);
                confidentButton.setBackgroundColor(0x9C27B0);
            }
        });

        confidentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confidentBg.setVisibility(view.VISIBLE);
                sadBg.setVisibility(view.GONE);
                anxiousBg.setVisibility(view.GONE);
                joyBg.setVisibility(view.GONE);

                confidenceImage.setVisibility(view.VISIBLE);
                joyImage.setVisibility(view.GONE);
                sadImage.setVisibility(view.GONE);
                anxietyImage.setVisibility(view.GONE);

                moodText.setText("Confidence");

                joyfulButton.setBackgroundColor(0x9C27B0);
                sadButton.setBackgroundColor(0x9C27B0);
                anxiousButton.setBackgroundColor(0x9C27B0);
                confidentButton.setBackgroundColor(0xFF6C8352);
            }
        });
    }

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_CODE = 2;

    // Function to check the current permissions which were given by the user,
    // and to request the user for missing permissions
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

    // Actions to take when user grants permissions
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

    // Background permissions must be requested only after other permissions have been granted
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

    // Start sensor service which reads heart rate data in the background
    private void startSensorService() {
        Intent serviceIntent = new Intent(this, SensorService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

    }


//    private ServiceConnection connection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            Log.i("MainActivity", "MainActivity connected to SensorService.");
//            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
//            sensorService = binder.getService();
//            isBound = true;
//
//            sensorService.registerHeartRateCallback(MainActivity.this);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            isBound = false;
//            sensorService.unregisterHeartRateCallback();
//        }
//    };
//
//    @Override
//    public void onHeartRateUpdate(int heartRate){
//        Log.i("MainActivity", "HEARTRATE: " + heartRate + " bpm");
//        String text = heartRate + " bpm";
//
//        runOnUiThread(() -> heartRateTextView.setText(text));
//    }
}

