package com.example.taskmaster.enums;

import androidx.annotation.NonNull;

public enum TaskStatusEnum {
    NEW("New"),
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress"),
    COMPLETE("Complete");

    private final String taskStatus;

    TaskStatusEnum(String taskState){
        this.taskStatus = taskState;
    }

    public String getTaskStatus(){
        return this.taskStatus;
    }

    public static TaskStatusEnum fromString(String possibleStatus){
        for (TaskStatusEnum task : TaskStatusEnum.values())
            if(task.taskStatus.equals(possibleStatus)){
                return task;
            }
        return null;
    }

    @NonNull
    @Override
    public String toString(){
        if (taskStatus == null){
            return "";
        }
        return taskStatus;
    }
}