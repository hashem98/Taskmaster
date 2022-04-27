package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UserSettingsActivity extends AppCompatActivity {
    SharedPreferences preferences;
    public static final String USER_USERNAME_TAG = "userUsername";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userUsername = preferences.getString(USER_USERNAME_TAG,"");
        if (!userUsername.isEmpty()) {
            EditText userUsernameEditText = (EditText) findViewById(R.id.editTextUserSettingsUsername);
            userUsernameEditText.setText(userUsername);
        }

        Button saveButton = findViewById(R.id.buttonUserSettingsSaveUsername);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor preferencesEditor = preferences.edit();
                EditText userUsernameEditText = (EditText) findViewById(R.id.editTextUserSettingsUsername);
                String userUsernameString = userUsernameEditText.getText().toString();
                System.out.println("this waas pushed" + userUsernameString);
                preferencesEditor.putString(USER_USERNAME_TAG, userUsernameString);
                preferencesEditor.apply();
                finish();
            }
        });
    }
}