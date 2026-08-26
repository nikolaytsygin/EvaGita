package com.eva.evagita.service;

import com.eva.evagita.exception.TaskNotFoundException;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void shouldCreateAndReadTaskThroughService() {
        Task task = new Task();
        task.setTitle("Integration task");
        task.setDescription("Created through service");

        Task createdTask = taskService.createTask(task);

        assertThat(createdTask.getId()).isNotNull();

        Task foundTask = taskService.getTaskById(createdTask.getId());

        assertThat(foundTask.getTitle()).isEqualTo("Integration task");
        assertThat(foundTask.getDescription())
                .isEqualTo("Created through service");
        assertThat(foundTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(foundTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(foundTask.getCreatedAt()).isNotNull();
        assertThat(foundTask.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldGetAllTasksThroughService() {
        Task firstTask = new Task();
        firstTask.setTitle("First integration task");

        Task secondTask = new Task();
        secondTask.setTitle("Second integration task");

        taskService.createTask(firstTask);
        taskService.createTask(secondTask);

        assertThat(taskService.getAllTasks())
                .hasSize(2)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder(
                        "First integration task",
                        "Second integration task"
                );
    }

    @Test
    void shouldUpdateTaskThroughService() {
        Task existingTask = new Task();
        existingTask.setTitle("Original title");
        existingTask.setDescription("Original description");
        existingTask.setStatus(TaskStatus.TODO);
        existingTask.setPriority(TaskPriority.LOW);
        existingTask.setDueDate(LocalDate.of(2026, 8, 30));

        Task createdTask = taskService.createTask(existingTask);

        Task updateRequest = new Task();
        updateRequest.setTitle("Updated title");
        updateRequest.setDescription("Updated description");
        updateRequest.setStatus(TaskStatus.DONE);
        updateRequest.setPriority(TaskPriority.HIGH);
        updateRequest.setDueDate(LocalDate.of(2026, 9, 15));

        Task updatedTask = taskService.updateTask(
                createdTask.getId(),
                updateRequest
        );

        assertThat(updatedTask.getTitle()).isEqualTo("Updated title");
        assertThat(updatedTask.getDescription())
                .isEqualTo("Updated description");
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(updatedTask.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(updatedTask.getDueDate())
                .isEqualTo(LocalDate.of(2026, 9, 15));

        Task foundTask = taskService.getTaskById(createdTask.getId());

        assertThat(foundTask.getTitle()).isEqualTo("Updated title");
        assertThat(foundTask.getDescription())
                .isEqualTo("Updated description");
        assertThat(foundTask.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(foundTask.getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void shouldDeleteTaskThroughService() {
        Task task = new Task();
        task.setTitle("Task to delete");

        Task createdTask = taskService.createTask(task);

        taskService.deleteTask(createdTask.getId());

        assertThatThrownBy(() ->
                taskService.getTaskById(createdTask.getId())
        )
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with id " + createdTask.getId() + " not found");

        assertThat(taskRepository.existsById(createdTask.getId()))
                .isFalse();
    }

    @Test
    void shouldRejectTaskWithEmptyTitle() {
        Task task = new Task();
        task.setTitle("");

        assertThatThrownBy(() -> taskService.createTask(task))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task title must not be empty");

        assertThat(taskRepository.count()).isZero();
    }
}
