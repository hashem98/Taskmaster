package com.example.taskmaster.activity;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taskmaster.R;
import com.example.taskmaster.adapter.TaskListRecycleReviewAdapter;
import com.example.taskmaster.database.AppDatabase;
import com.example.taskmaster.enums.TaskStatusEnum;
import com.example.taskmaster.model.Task;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    public static final String TASK_TITLE_TAG = "TASK";
    public static final String TASK_BODY_TAG = "BODY";
    public static final String TASK_STATE_TAG = "STATE";
    public static final String USER_USERNAME_TAG = "userUsername";
    List<Task> tasks = null;
    SharedPreferences preferences;
    TaskListRecycleReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Button buttonAddTask = findViewById(R.id.buttonAddTask);
        Button buttonAllTask = findViewById(R.id.buttonAllTask);
        Button buttonDeleteTask = findViewById(R.id.buttonDeleteAllTasks);

        tasks = AppDatabase.getInstance(getApplicationContext()).taskDao().getAll();




buttonDeleteTask.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        AppDatabase.getInstance(getApplicationContext()).
                taskDao().deleteAll( AppDatabase.getInstance(getApplicationContext()).taskDao().getAll());
        finish();
        startActivity(getIntent());

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
        setUpTaskListRecycleView();


    }
    @Override
    protected void onResume(){
        super.onResume();
        String userUsername = preferences.getString(UserSettingsActivity.USER_USERNAME_TAG,"No nickname");
        ((TextView) findViewById(R.id.textHomeUsernameView)).setText(getString(R.string.username_with_input, userUsername));

        Log.d(TAG, "hello " + userUsername );
        adapter.notifyDataSetChanged();
    }
    private void setUpTaskListRecycleView() {

        RecyclerView taskListRecycleReview = (RecyclerView) findViewById(R.id.homeTaskRecycleView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        taskListRecycleReview.setLayoutManager(layoutManager);

        adapter = new TaskListRecycleReviewAdapter(tasks, this);
        taskListRecycleReview.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

}