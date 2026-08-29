package com.eva.evagita.service;

import com.eva.evagita.exception.TaskNotFoundException;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task createTask(Task task) {
        validateTaskTitle(task);
        return taskRepository.save(task);
    }

    @Override
    public List<Task> getAllTasks(User user) {
        return taskRepository.findAllByUser(user);
    }

    @Override
    public Task getTaskById(Long id, User user) {
        return taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Override
    public Task updateTask(Long id, Task task, User user) {
        Task existingTask = getTaskById(id, user);

        validateTaskTitle(task);

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setPriority(task.getPriority());
        existingTask.setDueDate(task.getDueDate());

        return taskRepository.save(existingTask);
    }

    @Override
    public void deleteTask(Long id, User user) {
        Task existingTask = getTaskById(id, user);
        taskRepository.delete(existingTask);
    }

    private void validateTaskTitle(Task task) {
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Task title must not be empty");
        }
    }
}
