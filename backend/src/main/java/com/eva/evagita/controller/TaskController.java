package com.eva.evagita.controller;

import com.eva.evagita.dto.TaskRequest;
import com.eva.evagita.dto.TaskResponse;
import com.eva.evagita.exception.UserNotFoundException;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(
            TaskService taskService,
            UserRepository userRepository
    ) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<TaskResponse> getAllTasks() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        return taskService.getAllTasks(currentUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        return toResponse(taskService.getTaskById(id, currentUser));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
            @Valid @RequestBody TaskRequest request
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        Task task = toEntity(request);
        task.setUser(currentUser);

        return toResponse(taskService.createTask(task));
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        Task task = toEntity(request);
        return toResponse(taskService.updateTask(id, task, currentUser));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        taskService.deleteTask(id, currentUser);
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException(authentication.getName()));
    }

    private Task toEntity(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        return task;
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }
}
