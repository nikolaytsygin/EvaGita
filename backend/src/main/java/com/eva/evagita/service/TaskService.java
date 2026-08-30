package com.eva.evagita.service;

import com.eva.evagita.model.Tag;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.User;

import java.util.List;

public interface TaskService {

    Task createTask(Task task);

    List<Task> getAllTasks(User user);

    List<Task> searchTasks(String title, User user);

    List<Task> searchTasksByDescription(String description, User user);

    Task getTaskById(Long id, User user);

    Task updateTask(Long id, Task task, User user);

    com.eva.evagita.model.Project getProjectForUser(Long projectId, User user);

    void addTagToTask(Long taskId, Long tagId, User user);

    List<Tag> getTaskTags(Long taskId, User user);

    void removeTagFromTask(Long taskId, Long tagId, User user);

    void deleteTask(Long id, User user);
}
