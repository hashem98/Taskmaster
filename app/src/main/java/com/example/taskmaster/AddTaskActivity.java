package com.example.taskmaster;

import static android.graphics.Color.parseColor;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AddTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        Button addTaskButton = findViewById(R.id.buttonAddTaskTaskActivity);
        TextView textView=(TextView)findViewById(R.id.textViewAddTaskSubmit);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText(R.string.Submitted);
                textView.setTextColor(parseColor("#357C3C"));
            }
        });

    }
}