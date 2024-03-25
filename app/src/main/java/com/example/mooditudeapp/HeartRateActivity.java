package com.example.mooditudeapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.Context;

import android.os.IBinder;

public class HeartRateActivity extends AppCompatActivity implements SensorService.HeartRateCallback {

    SensorService sensorService;
    boolean isBound = false;

    private ImageView heartImage;
    private Button measureButton;
    private TextView heartRateTextView;
    private TextView bpmTextView;
    private Animation heartBeat;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); //idk bout this one
        setContentView(R.layout.activity_heart_rate);
        getSupportActionBar().setTitle("Heart Rate Page");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        heartImage = findViewById(R.id.heartImage);
        heartRateTextView = findViewById(R.id.heartRateTextView);
        bpmTextView = findViewById(R.id.bpmTextView);

        heartBeat = AnimationUtils.loadAnimation(this,R.anim.heart_beat);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, SensorService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        heartImage.setAnimation(heartBeat);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i("HeartRateActivity", "HeartRateActivity connected to SensorService.");
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            sensorService = binder.getService();
            isBound = true;

            sensorService.registerHeartRateCallback(HeartRateActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
            sensorService.unregisterHeartRateCallback();
        }
    };

    @Override
    public void onHeartRateUpdate(int heartRate){
        Log.i("HeartRateActivity", "HEARTRATE: " + heartRate + " bpm");
        String text = heartRate + "";

        runOnUiThread(() -> heartRateTextView.setText(text));
    }
}