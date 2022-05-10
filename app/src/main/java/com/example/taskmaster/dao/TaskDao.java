package com.example.taskmaster.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.taskmaster.model.Task;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM task_data")
    List<Task> getAll();

    @Query("SELECT * FROM task_data WHERE id = :id")
    Task getTaskByID(Long id);

    @Insert
    Long insertTask(Task task);
}