package com.eva.evagita.controller;

import com.eva.evagita.dto.TaskRequest;
import com.eva.evagita.dto.TaskResponse;
import com.eva.evagita.dto.TaskStatusStatisticsResponse;
import com.eva.evagita.exception.UserNotFoundException;
import com.eva.evagita.model.Tag;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public List<TaskResponse> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        List<Task> tasks;

        if (status != null
                || priority != null
                || projectId != null
                || tagId != null
                || dueDateFrom != null
                || dueDateTo != null) {

            tasks = taskService.getTasksByFilters(
                    status,
                    priority,
                    projectId,
                    tagId,
                    dueDateFrom,
                    dueDateTo,
                    currentUser
            );
        } else {
            tasks = taskService.getAllTasks(currentUser);
        }

        return tasks.stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/search")
    public List<TaskResponse> searchTasks(
            @RequestParam String title
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        return taskService.searchTasks(title, currentUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/search/description")
    public List<TaskResponse> searchTasksByDescription(
            @RequestParam String description
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        return taskService.searchTasksByDescription(description, currentUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/statistics/status")
    public TaskStatusStatisticsResponse getTaskStatusStatistics() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        return new TaskStatusStatisticsResponse(
                taskService.countTasksByStatus(TaskStatus.TODO, currentUser),
                taskService.countTasksByStatus(
                        TaskStatus.IN_PROGRESS,
                        currentUser
                ),
                taskService.countTasksByStatus(TaskStatus.DONE, currentUser)
        );
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

        Task task = toEntity(request, currentUser);
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

        Task task = toEntity(request, currentUser);

        return toResponse(taskService.updateTask(id, task, currentUser));
    }

    @PostMapping("/{taskId}/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addTagToTask(
            @PathVariable Long taskId,
            @PathVariable Long tagId
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        taskService.addTagToTask(taskId, tagId, currentUser);
    }

    @GetMapping("/{taskId}/tags")
    public List<Tag> getTaskTags(
            @PathVariable Long taskId
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        return taskService.getTaskTags(taskId, currentUser);
    }

    @DeleteMapping("/{taskId}/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTagFromTask(
            @PathVariable Long taskId,
            @PathVariable Long tagId
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = getCurrentUser(authentication);

        taskService.removeTagFromTask(taskId, tagId, currentUser);
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

    private Task toEntity(TaskRequest request, User user) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        if (request.getProjectId() != null) {
            task.setProject(
                    taskService.getProjectForUser(
                            request.getProjectId(),
                            user
                    )
            );
        }

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

        if (task.getProject() != null) {
            response.setProjectId(task.getProject().getId());
        }

        response.setTags(task.getTags());

        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        return response;
    }
}
