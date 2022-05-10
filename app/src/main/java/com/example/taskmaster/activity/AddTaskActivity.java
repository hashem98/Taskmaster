package com.example.taskmaster.activity;


import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


import androidx.room.Room;


import com.example.taskmaster.R;
import com.example.taskmaster.database.AppDatabase;
import com.example.taskmaster.enums.TaskStatusEnum;
import com.example.taskmaster.model.Task;

import java.util.Date;

public class AddTaskActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);






        Spinner taskStateSpinner = (Spinner) findViewById(R.id.editAddTaskStateSpinner);
        taskStateSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TaskStatusEnum.values()));

        //Buttons
        Button addTaskButton = (Button) findViewById(R.id.buttonAddTaskTaskActivity);
        addTaskButton.setOnClickListener(v -> {
            Task newTask = new Task(
                    ((EditText)findViewById(R.id.editTextTaskTitle)).getText().toString(),
                    ((EditText)findViewById(R.id.editTextTaskDescription)).getText().toString(),
                    TaskStatusEnum.fromString(taskStateSpinner.getSelectedItem().toString())
            );

            Long newTaskk = AppDatabase.getInstance(getApplicationContext()).taskDao().insertTask(newTask);
            System.out.println("******************** Student ID = " + newTaskk + " ************************");
            startActivity(new Intent(getApplicationContext() , HomeActivity.class));

        });
    }
}

