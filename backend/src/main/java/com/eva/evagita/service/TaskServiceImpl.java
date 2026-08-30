package com.eva.evagita.service;

import com.eva.evagita.exception.TagNotFoundException;
import com.eva.evagita.exception.TaskNotFoundException;
import com.eva.evagita.model.Project;
import com.eva.evagita.model.Tag;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.ProjectRepository;
import com.eva.evagita.repository.TagRepository;
import com.eva.evagita.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;

    @Autowired
    public TaskServiceImpl(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            TagRepository tagRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.tagRepository = tagRepository;
    }

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = null;
        this.tagRepository = null;
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
    public List<Task> searchTasks(String title, User user) {
        return taskRepository.findAllByUserAndTitleContainingIgnoreCase(
                user,
                title
        );
    }

    @Override
    public List<Task> searchTasksByDescription(String description, User user) {
        if (description == null || description.isBlank()) {
            return List.of();
        }

        return taskRepository.findAllByUserAndDescriptionContainingIgnoreCase(
                user,
                description
        );
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
        existingTask.setProject(task.getProject());

        return taskRepository.save(existingTask);
    }

    @Override
    public void addTagToTask(Long taskId, Long tagId, User user) {
        Task task = getTaskById(taskId, user);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() ->
                        new TagNotFoundException(
                                "Tag not found with id: " + tagId
                        ));

        task.getTags().add(tag);
        taskRepository.save(task);
    }

    @Override
    public List<Tag> getTaskTags(Long taskId, User user) {
        Task task = getTaskById(taskId, user);

        return List.copyOf(task.getTags());
    }

    @Override
    public void removeTagFromTask(Long taskId, Long tagId, User user) {
        Task task = getTaskById(taskId, user);

        boolean removed = task.getTags()
                .removeIf(tag -> tag.getId().equals(tagId));

        if (!removed) {
            throw new TagNotFoundException(
                    "Tag with id " + tagId +
                    " is not attached to task " + taskId
            );
        }

        taskRepository.save(task);
    }

    @Override
    public void deleteTask(Long id, User user) {
        Task existingTask = getTaskById(id, user);
        taskRepository.delete(existingTask);
    }

    public Project getProjectForUser(Long projectId, User user) {
        if (projectId == null) {
            return null;
        }

        return projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found"));
    }

    private void validateTaskTitle(Task task) {
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Task title must not be empty");
        }
    }
}
