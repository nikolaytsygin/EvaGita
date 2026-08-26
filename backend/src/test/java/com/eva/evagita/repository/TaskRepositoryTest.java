package com.eva.evagita.repository;

import org.junit.jupiter.api.BeforeEach;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskRepositoryTest {

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
}