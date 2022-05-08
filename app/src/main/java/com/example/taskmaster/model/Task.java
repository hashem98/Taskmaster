package com.example.taskmaster.model;

import com.example.taskmaster.enums.state;

public class Task {
    String title;
    String body;
    com.example.taskmaster.enums.state state;

    public Task(String title, String body, state state) {
        this.title = title;
        this.body = body;
        this.state = state;
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

    public com.example.taskmaster.enums.state getState() {
        return state;
    }

    public void setState(  com.example.taskmaster.enums.state     state) {
        this.state = state;
    }
}