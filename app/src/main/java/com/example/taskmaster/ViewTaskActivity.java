package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ViewTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);

        Intent callingIntent = getIntent();
        String taskTitleString = null;
        String taskBodyString = null;
        String taskStateEnum = null;

        if (callingIntent != null){
            taskTitleString = callingIntent.getStringExtra(HomeActivity.TASK_TITLE_TAG);
            taskBodyString = callingIntent.getStringExtra(HomeActivity.TASK_BODY_TAG);
            taskStateEnum = callingIntent.getStringExtra(String.valueOf(HomeActivity.TASK_STATE_TAG));
        }

        TextView taskViewTitleView = (TextView) findViewById(R.id.textTaskViewTitle);
        TextView taskViewBodyView = (TextView) findViewById(R.id.textTaskViewBody);
        TextView taskViewStateView = (TextView) findViewById(R.id.textTaskViewStatus);


        if (taskTitleString != null){
            taskViewTitleView.setText(taskTitleString);
        } else {
            taskViewTitleView.setText(R.string.no_task_name);
        }

        if (taskBodyString != null){
            taskViewBodyView.setText(taskBodyString);
        } else {
            taskViewBodyView.setText(R.string.no_task_name);
        }

        if (taskStateEnum != null){
            taskViewStateView.setText(taskStateEnum);
        } else {
            taskViewStateView.setText(R.string.no_task_state);
        }

    }
}