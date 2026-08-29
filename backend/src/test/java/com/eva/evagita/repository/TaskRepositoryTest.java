package com.eva.evagita.repository;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    private User anotherUser;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(
                "task-test-user",
                "task-test@example.com",
                "password"
        );

        anotherUser = new User(
                "another-task-user",
                "another-task@example.com",
                "password"
        );

        testUser = userRepository.save(testUser);
        anotherUser = userRepository.save(anotherUser);
    }

    @Test
    void shouldSaveAndReadTask() {
        Task task = new Task();
        task.setTitle("Test task");
        task.setDescription("Task created in integration test");
        task.setUser(testUser);

        Task savedTask = taskRepository.save(task);

        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(savedTask.getUser()).isEqualTo(testUser);
        assertThat(savedTask.getCreatedAt()).isNotNull();
        assertThat(savedTask.getUpdatedAt()).isNotNull();

        Task foundTask = taskRepository.findById(savedTask.getId())
                .orElseThrow();

        assertThat(foundTask.getTitle()).isEqualTo("Test task");
        assertThat(foundTask.getDescription())
                .isEqualTo("Task created in integration test");
        assertThat(foundTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(foundTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(foundTask.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(foundTask.getCreatedAt()).isNotNull();
        assertThat(foundTask.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindAllTasksForSpecificUser() {
        Task firstTask = new Task();
        firstTask.setTitle("First user task");
        firstTask.setUser(testUser);

        Task secondTask = new Task();
        secondTask.setTitle("Second user task");
        secondTask.setUser(testUser);

        Task anotherUsersTask = new Task();
        anotherUsersTask.setTitle("Another user task");
        anotherUsersTask.setUser(anotherUser);

        taskRepository.saveAll(List.of(
                firstTask,
                secondTask,
                anotherUsersTask
        ));

        assertThat(taskRepository.findAllByUser(testUser))
                .hasSize(2)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder(
                        "First user task",
                        "Second user task"
                );

        assertThat(taskRepository.findAllByUser(anotherUser))
                .hasSize(1)
                .extracting(Task::getTitle)
                .containsExactly("Another user task");
    }

    @Test
    void shouldFindTaskByIdAndUser() {
        Task task = new Task();
        task.setTitle("User task");
        task.setUser(testUser);

        Task savedTask = taskRepository.save(task);

        assertThat(taskRepository.findByIdAndUser(
                savedTask.getId(),
                testUser
        )).isPresent();

        assertThat(taskRepository.findByIdAndUser(
                savedTask.getId(),
                anotherUser
        )).isEmpty();
    }

    @Test
    void shouldCheckTaskExistence() {
        Task task = new Task();
        task.setTitle("Existing task");
        task.setUser(testUser);

        Task savedTask = taskRepository.save(task);

        assertThat(taskRepository.existsById(savedTask.getId()))
                .isTrue();
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task();
        task.setTitle("Task to delete");
        task.setUser(testUser);

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
        task.setUser(testUser);

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
        assertThat(updatedTask.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(updatedTask.getUpdatedAt())
                .isAfterOrEqualTo(originalUpdatedAt);

        Task foundTask = taskRepository.findById(savedTask.getId())
                .orElseThrow();

        assertThat(foundTask.getTitle()).isEqualTo("Updated title");
        assertThat(foundTask.getDescription())
                .isEqualTo("Updated description");
        assertThat(foundTask.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(foundTask.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(foundTask.getUser().getId()).isEqualTo(testUser.getId());
    }
}
