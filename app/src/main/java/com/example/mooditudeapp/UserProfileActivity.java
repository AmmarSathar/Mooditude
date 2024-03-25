package com.example.mooditudeapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserProfileActivity extends AppCompatActivity {

    private static final String PREF_NAME = "UserProfilePrefs";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_WEIGHT = "weight";

    private EditText fullNameEditText;
    private EditText userNameEditText;
    private EditText genderEditText;
    private EditText birthdayEditText;
    private EditText heightEditText;
    private EditText weightEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fullNameEditText = findViewById(R.id.fullNameEditText);
        userNameEditText = findViewById(R.id.userNameEditText);
        genderEditText = findViewById(R.id.genderEditText);
        birthdayEditText = findViewById(R.id.birthdayEditText);
        heightEditText = findViewById(R.id.heightEditText);
        weightEditText = findViewById(R.id.weightEditText);
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });

        // Load user profile information
        loadUserProfile();
    }

    private void saveUserProfile() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_FULL_NAME, fullNameEditText.getText().toString());
        editor.putString(KEY_USER_NAME, userNameEditText.getText().toString());
        editor.putString(KEY_GENDER, genderEditText.getText().toString());
        editor.putString(KEY_BIRTHDAY, birthdayEditText.getText().toString());
        editor.putString(KEY_HEIGHT, heightEditText.getText().toString());
        editor.putString(KEY_WEIGHT, weightEditText.getText().toString());
        editor.apply();

        Toast.makeText(UserProfileActivity.this, "Saved", Toast.LENGTH_SHORT).show();
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        fullNameEditText.setText(prefs.getString(KEY_FULL_NAME, ""));
        userNameEditText.setText(prefs.getString(KEY_USER_NAME, ""));
        genderEditText.setText(prefs.getString(KEY_GENDER, ""));
        birthdayEditText.setText(prefs.getString(KEY_BIRTHDAY, ""));
        heightEditText.setText(prefs.getString(KEY_HEIGHT, ""));
        weightEditText.setText(prefs.getString(KEY_WEIGHT, ""));
    }
}
