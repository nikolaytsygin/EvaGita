package com.eva.evagita.service;

import com.eva.evagita.model.Task;
import com.eva.evagita.model.User;

import java.util.List;

public interface TaskService {

    Task createTask(Task task);

    List<Task> getAllTasks(User user);

    Task getTaskById(Long id, User user);

    Task updateTask(Long id, Task task, User user);

    void deleteTask(Long id, User user);
}
