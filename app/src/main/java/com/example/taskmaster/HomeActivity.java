package com.example.taskmaster;

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

import com.example.taskmaster.adapter.TaskListRecycleReviewAdapter;
import com.example.taskmaster.enums.state;
import com.example.taskmaster.model.Task;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    public static final String TASK_TITLE_TAG = "TASK";
    public static final String TASK_BODY_TAG = "BODY";
    public static final String TASK_STATE_TAG = "STATE";
    public static final String USER_USERNAME_TAG = "userUsername";
    SharedPreferences preferences;
    TaskListRecycleReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Button buttonAddTask = findViewById(R.id.buttonAddTask);
        Button buttonAllTask = findViewById(R.id.buttonAllTask);







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
    }
    private void setUpTaskListRecycleView() {

        RecyclerView taskListRecycleReview = (RecyclerView) findViewById(R.id.homeTaskRecycleView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        //((LinearLayoutManager)layoutManager).setOrientation(LinearLayoutManager.HORIZONTAL);
        taskListRecycleReview.setLayoutManager(layoutManager);


        List<Task> taskList = new ArrayList<Task>();
        taskList.add(new Task("tutorial", "Watch your tutorial man!", state.COMPLETE));
        taskList.add(new Task("challenges", "Do your daily challenges ", state.IN_PROGRESS));
        taskList.add(new Task("Reading", "Read the night away!", state.NEW));
        taskList.add(new Task("plants", "water your plants man.", state.IN_PROGRESS));
        taskList.add(new Task("sport", "Do your sport man", state.COMPLETE));

        adapter = new TaskListRecycleReviewAdapter(taskList, this);

        taskListRecycleReview.setAdapter(adapter);

    }
}