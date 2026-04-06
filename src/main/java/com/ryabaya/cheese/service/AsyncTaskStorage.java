package com.ryabaya.cheese.service;

import com.ryabaya.cheese.entity.AsyncTask;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AsyncTaskStorage {

    private final Map<String, AsyncTask> taskStatuses = new ConcurrentHashMap<>();

    public void saveTask(AsyncTask task) {
        taskStatuses.put(task.getTaskId(), task);
    }

    public AsyncTask getTask(String taskId) {
        return taskStatuses.get(taskId);
    }

    public Map<String, AsyncTask> getAllTasks() {
        return taskStatuses;
    }
}
