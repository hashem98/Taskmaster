package com.example.taskmaster;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {
    public static final String TASK_TITLE_TAG = "TASK";
    public static final String USER_USERNAME_TAG = "userUsername";
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Button buttonAddTask = findViewById(R.id.buttonAddTask);
        Button buttonAllTask = findViewById(R.id.buttonAllTask);
        Button buttonViewTaskOne = findViewById(R.id.buttonViewTaskOneHome);
        Button buttonViewTaskTwo = findViewById(R.id.buttonViewTaskTwoHome);
        Button buttonViewTaskThree = findViewById(R.id.buttonViewTaskThreeHome);


        buttonViewTaskOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String taskName = ((EditText) findViewById(R.id.editTextViewTaskTitle)).getText().toString();

                Intent goToViewTaskPageIntent = new Intent(HomeActivity.this, ViewTaskActivity.class);
                goToViewTaskPageIntent.putExtra(TASK_TITLE_TAG, "Task 1");
                startActivity(goToViewTaskPageIntent);
            }
        });

        buttonViewTaskTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String taskName = ((EditText) findViewById(R.id.editTextViewTaskTitle)).getText().toString();

                Intent goToViewTaskPageIntent = new Intent(HomeActivity.this, ViewTaskActivity.class);
                goToViewTaskPageIntent.putExtra(TASK_TITLE_TAG, "Task 2");
                startActivity(goToViewTaskPageIntent);
            }
        });

        buttonViewTaskThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String taskName = ((EditText) findViewById(R.id.editTextViewTaskTitle)).getText().toString();

                Intent goToViewTaskPageIntent = new Intent(HomeActivity.this, ViewTaskActivity.class);
                goToViewTaskPageIntent.putExtra(TASK_TITLE_TAG, "Task 3");
                startActivity(goToViewTaskPageIntent);
            }
        });

        buttonAllTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToAllTaskActivityIntent = new Intent(HomeActivity.this, AllTasksActivity.class);
                startActivity(goToAllTaskActivityIntent);
            }
        });

        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToAddTaskActivityIntent = new Intent(HomeActivity.this, AddTaskActivity.class);
                startActivity(goToAddTaskActivityIntent);
            }

        });


        ImageView userSettingsImageView = (ImageView) findViewById(R.id.userSettingsImage);

        userSettingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToUserSettingsIntent = new Intent(HomeActivity.this, UserSettingsActivity.class);
                startActivity(goToUserSettingsIntent);
            }
        });

    }
    @Override
    protected void onResume(){
        super.onResume();
        String userUsername = preferences.getString(UserSettingsActivity.USER_USERNAME_TAG,"No nickname");
        ((TextView) findViewById(R.id.textHomeUsernameView)).setText(getString(R.string.username_with_input, userUsername));

        Log.d(TAG, "hello " + userUsername );
    }
}