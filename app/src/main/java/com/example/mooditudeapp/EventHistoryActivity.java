package com.example.mooditudeapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class EventHistoryActivity extends AppCompatActivity {

    CalendarView calendarView;
    Calendar calendar;

    private TextView heartRateTextView;
    private TextView joyTextView;
    private TextView sadnessTextView;
    private TextView anxietyTextView;
    private TextView confidenceTextView;

    private TextView heartRateVal;
    private TextView joyVal;
    private TextView sadVal;
    private TextView anxietyVal;
    private TextView confidenceVal;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_history);
        getSupportActionBar().setTitle("Event History Page");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        calendarView = findViewById(R.id.calendarView);
        calendar = Calendar.getInstance();

        heartRateVal = findViewById(R.id.heartRateVal);
        joyVal = findViewById(R.id.joyVal);
        sadVal = findViewById(R.id.sadVal);
        anxietyVal = findViewById(R.id.anxietyVal);
        confidenceVal = findViewById(R.id.confidenceVal);

        //setDate(1,1,2024);
        //getDate();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                Toast.makeText(EventHistoryActivity.this, day + "/" + (month+1) + "/" + year, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //    public void getDate(){
//        long date = calendarView.getDate();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
//        calendar.setTimeInMillis(date);
//        String selected_date = simpleDateFormat.format(calendar.getTime());
//        Toast.makeText(this, selected_date, Toast.LENGTH_SHORT).show();
//    }
//
//    public void setDate(int day, int month, int year){
//        calendar.set(Calendar.YEAR, year);
//        calendar.set(Calendar.MONTH, month - 1);
//        calendar.set(Calendar.DAY_OF_MONTH, day);
//        long milli = calendar.getTimeInMillis();
//        calendarView.setDate(milli);
//    }
}