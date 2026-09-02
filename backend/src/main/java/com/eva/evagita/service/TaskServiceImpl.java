package com.eva.evagita.service;

import com.eva.evagita.event.TaskEvent;
import com.eva.evagita.messaging.TaskEventProducer;
import com.eva.evagita.exception.TagNotFoundException;
import com.eva.evagita.exception.TaskNotFoundException;
import com.eva.evagita.model.Project;
import com.eva.evagita.model.Tag;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.ProjectRepository;
import com.eva.evagita.repository.TagRepository;
import com.eva.evagita.repository.TaskRepository;
import com.eva.evagita.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;
    private final TaskEventProducer taskEventProducer;

    @Autowired
    public TaskServiceImpl(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            TagRepository tagRepository,
            TaskEventProducer taskEventProducer
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.tagRepository = tagRepository;
        this.taskEventProducer = taskEventProducer;
    }

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = null;
        this.tagRepository = null;
        this.taskEventProducer = null;
    }

    @Override
    public Task createTask(Task task) {
        validateTaskTitle(task);
        Task savedTask = taskRepository.save(task);

        taskEventProducer.send(
                new TaskEvent(
                        com.eva.evagita.model.NotificationType.TASK_CREATED,
                        savedTask.getUser().getId(),
                        savedTask.getId(),
                        savedTask.getTitle()
                )
        );

        return savedTask;
    }

    @Override
    public List<Task> getAllTasks(User user) {
        return taskRepository.findAllByUser(user);
    }

    @Override
    public long countTasks(User user) {
        return taskRepository.countByUser(user);
    }

    @Override
    public long countTasksByStatus(TaskStatus status, User user) {
        return taskRepository.countByUserAndStatus(user, status);
    }

    @Override
    public long countTasksByPriority(TaskPriority priority, User user) {
        return taskRepository.countByUserAndPriority(user, priority);
    }

    @Override
    public long countOverdueTasks(User user) {
        return taskRepository.countByUserAndDueDateBeforeAndStatusNot(
                user,
                LocalDate.now(),
                TaskStatus.DONE
        );
    }

    @Override
    public List<Task> getTasksByProject(Long projectId, User user) {
        return taskRepository.findAllByUserAndProject_Id(user, projectId);
    }

    @Override
    public List<Task> getTasksByTag(Long tagId, User user) {
        return taskRepository.findAllByUserAndTags_Id(user, tagId);
    }

    @Override
    public List<Task> getTasksByStatus(TaskStatus status, User user) {
        return taskRepository.findAllByUserAndStatus(user, status);
    }

    @Override
    public List<Task> getTasksByPriority(TaskPriority priority, User user) {
        return taskRepository.findAllByUserAndPriority(user, priority);
    }

    @Override
    public List<Task> getTasksByDueDate(
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            User user
    ) {
        if (dueDateFrom != null && dueDateTo != null) {
            return taskRepository.findAllByUserAndDueDateBetween(
                    user,
                    dueDateFrom,
                    dueDateTo
            );
        }

        if (dueDateFrom != null) {
            return taskRepository.findAllByUserAndDueDateGreaterThanEqual(
                    user,
                    dueDateFrom
            );
        }

        if (dueDateTo != null) {
            return taskRepository.findAllByUserAndDueDateLessThanEqual(
                    user,
                    dueDateTo
            );
        }

        return taskRepository.findAllByUser(user);
    }

    @Override
    public List<Task> getTasksByFilters(
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            Long tagId,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            User user
    ) {
        return taskRepository.findAll(
                TaskSpecification.byFilters(
                        status,
                        priority,
                        projectId,
                        tagId,
                        dueDateFrom,
                        dueDateTo,
                        user
                )
        );
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

        Task savedTask = taskRepository.save(existingTask);

        taskEventProducer.send(
                new TaskEvent(
                        savedTask.getStatus() == TaskStatus.DONE
                                ? com.eva.evagita.model.NotificationType.TASK_COMPLETED
                                : com.eva.evagita.model.NotificationType.TASK_UPDATED,
                        savedTask.getUser().getId(),
                        savedTask.getId(),
                        savedTask.getTitle()
                )
        );

        return savedTask;
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
