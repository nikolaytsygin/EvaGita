package com.eva.evagita.repository;

import com.eva.evagita.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

    }
    
    @Test
    void shouldSaveAndReadTask() {
        Task task = new Task();
        task.setTitle("Test task");
        task.setDescription("Task created in integration test");

        Task savedTask = taskRepository.save(task);

        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(savedTask.getCreatedAt()).isNotNull();
        assertThat(savedTask.getUpdatedAt()).isNotNull();

        Task foundTask = taskRepository.findById(savedTask.getId())
                .orElseThrow();

        assertThat(foundTask.getTitle()).isEqualTo("Test task");
        assertThat(foundTask.getDescription())
                .isEqualTo("Task created in integration test");
        assertThat(foundTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(foundTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(foundTask.getCreatedAt()).isNotNull();
        assertThat(foundTask.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindAllTasks() {
        Task firstTask = new Task();
        firstTask.setTitle("First task");

        Task secondTask = new Task();
        secondTask.setTitle("Second task");

        taskRepository.save(firstTask);
        taskRepository.save(secondTask);

        assertThat(taskRepository.findAll())
                .hasSize(2)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("First task", "Second task");
    }

    @Test
    void shouldCheckTaskExistence() {
        Task task = new Task();
        task.setTitle("Existing task");

        Task savedTask = taskRepository.save(task);

        assertThat(taskRepository.existsById(savedTask.getId()))
                .isTrue();
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task();
        task.setTitle("Task to delete");

        Task savedTask = taskRepository.save(task);

        taskRepository.deleteById(savedTask.getId());

        assertThat(taskRepository.existsById(savedTask.getId()))
                .isFalse();

        assertThat(taskRepository.findById(savedTask.getId()))
                .isEmpty();
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task();
        task.setTitle("Original title");
        task.setDescription("Original description");

        Task savedTask = taskRepository.saveAndFlush(task);

        assertThat(savedTask.getUpdatedAt()).isNotNull();

        var originalUpdatedAt = savedTask.getUpdatedAt();

        savedTask.setTitle("Updated title");
        savedTask.setDescription("Updated description");
        savedTask.setStatus(TaskStatus.DONE);
        savedTask.setPriority(TaskPriority.HIGH);

        Task updatedTask = taskRepository.saveAndFlush(savedTask);

        assertThat(updatedTask.getTitle()).isEqualTo("Updated title");
        assertThat(updatedTask.getDescription())
                .isEqualTo("Updated description");
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(updatedTask.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(updatedTask.getUpdatedAt())
                .isAfterOrEqualTo(originalUpdatedAt);

        Task foundTask = taskRepository.findById(savedTask.getId())
                .orElseThrow();

        assertThat(foundTask.getTitle()).isEqualTo("Updated title");
        assertThat(foundTask.getDescription())
                .isEqualTo("Updated description");
        assertThat(foundTask.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(foundTask.getPriority()).isEqualTo(TaskPriority.HIGH);
    }

}