package com.example.taskmaster.model;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


import com.example.taskmaster.enums.TaskStatusEnum;

import java.util.Date;



@Entity(tableName = "task_data")
public class Task {

    @PrimaryKey(autoGenerate = true)
    Long id;
    @ColumnInfo(name = "title")
    String title;
    @ColumnInfo(name = "body")
    String body;
    @ColumnInfo(name = "state")
    TaskStatusEnum taskStatusEnum;
//    @ColumnInfo(name = "date")
//    java.util.Date dateCreated;


    public Task(String title, String body, TaskStatusEnum taskStatusEnum) {
        this.title = title;
        this.body = body;
        this.taskStatusEnum = taskStatusEnum;
//        this.dateCreated = dateCreated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public TaskStatusEnum getTaskStatusEnum() {
        return taskStatusEnum;
    }

    public void setTaskStatusEnum(TaskStatusEnum taskStatusEnum) {
        this.taskStatusEnum = taskStatusEnum;
    }

//    public Date getDateCreated() {
//        return dateCreated;
//    }
//
//    public void setDateCreated(Date dateCreated) {
//        this.dateCreated = dateCreated;
//    }
}