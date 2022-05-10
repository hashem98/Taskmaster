package com.example.taskmaster.adapter;

import static com.example.taskmaster.activity.HomeActivity.TASK_BODY_TAG;
import static com.example.taskmaster.activity.HomeActivity.TASK_STATE_TAG;
import static com.example.taskmaster.activity.HomeActivity.TASK_TITLE_TAG;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.taskmaster.R;
import com.example.taskmaster.activity.ViewTaskActivity;
import com.example.taskmaster.model.Task;

import java.util.List;

public class TaskListRecycleReviewAdapter extends RecyclerView.Adapter<TaskListRecycleReviewAdapter.TaskListViewHolder> {

    List<Task> taskList;
    Context callingActivity;

    public TaskListRecycleReviewAdapter(List<Task> taskList, Context callingActivity) {
        this.taskList = taskList;
        this.callingActivity = callingActivity;
    }

    @NonNull
    @Override
    public TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View taskFragment = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_task_list, parent, false);

        return new TaskListViewHolder(taskFragment);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListViewHolder holder, int position) {
        TextView taskFragmentTextView = (TextView) holder.itemView.findViewById(R.id.taskFragmentTextViewHome);
        taskFragmentTextView.setText(taskList.get(position).getTitle());
        String taskTitle = taskList.get(position).getTitle();
        String taskBody = taskList.get(position).getBody();
        String taskState = taskList.get(position).getTaskStatusEnum().toString();


        View taskViewHolder = holder.itemView;
        taskViewHolder.setOnClickListener(view -> {
            Intent goToViewTaskFormIntent = new Intent(callingActivity, ViewTaskActivity.class);
            goToViewTaskFormIntent.putExtra(TASK_BODY_TAG, taskBody);
            goToViewTaskFormIntent.putExtra(TASK_TITLE_TAG, taskTitle);
            goToViewTaskFormIntent.putExtra(TASK_STATE_TAG, taskState);
            callingActivity.startActivity(goToViewTaskFormIntent);
        });


    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskListViewHolder extends RecyclerView.ViewHolder {

        public TaskListViewHolder(View fragmentItemView) {
            super(fragmentItemView);
        }

    }
}