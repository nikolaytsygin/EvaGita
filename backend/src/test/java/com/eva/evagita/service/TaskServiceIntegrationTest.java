package com.eva.evagita.service;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.exception.TaskNotFoundException;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.TaskRepository;
import com.eva.evagita.repository.UserRepository;
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
class TaskServiceIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private TaskService taskService;

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
                "service-test-user",
                "service-test@example.com",
                "password"
        );

        anotherUser = new User(
                "another-service-user",
                "another-service@example.com",
                "password"
        );

        testUser = userRepository.save(testUser);
        anotherUser = userRepository.save(anotherUser);
    }

    @Test
    void shouldCreateAndReadTaskThroughService() {
        Task task = new Task();
        task.setTitle("Integration task");
        task.setDescription("Created through service");
        task.setUser(testUser);

        Task createdTask = taskService.createTask(task);

        assertThat(createdTask.getId()).isNotNull();
        assertThat(createdTask.getUser().getId()).isEqualTo(testUser.getId());

        Task foundTask = taskService.getTaskById(
                createdTask.getId(),
                testUser
        );

        assertThat(foundTask.getTitle()).isEqualTo("Integration task");
        assertThat(foundTask.getDescription())
                .isEqualTo("Created through service");
        assertThat(foundTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(foundTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(foundTask.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(foundTask.getCreatedAt()).isNotNull();
        assertThat(foundTask.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldGetOnlyTasksOfSpecificUserThroughService() {
        Task firstTask = new Task();
        firstTask.setTitle("First user task");
        firstTask.setUser(testUser);

        Task secondTask = new Task();
        secondTask.setTitle("Second user task");
        secondTask.setUser(testUser);

        Task anotherUsersTask = new Task();
        anotherUsersTask.setTitle("Another user task");
        anotherUsersTask.setUser(anotherUser);

        taskService.createTask(firstTask);
        taskService.createTask(secondTask);
        taskService.createTask(anotherUsersTask);

        assertThat(taskService.getAllTasks(testUser))
                .hasSize(2)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder(
                        "First user task",
                        "Second user task"
                );

        assertThat(taskService.getAllTasks(anotherUser))
                .hasSize(1)
                .extracting(Task::getTitle)
                .containsExactly("Another user task");
    }

    @Test
    void shouldNotReadTaskOfAnotherUser() {
        Task task = new Task();
        task.setTitle("Private task");
        task.setUser(testUser);

        Task createdTask = taskService.createTask(task);

        assertThatThrownBy(() ->
                taskService.getTaskById(
                        createdTask.getId(),
                        anotherUser
                )
        )
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage(
                        "Task with id " + createdTask.getId() + " not found"
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
        existingTask.setUser(testUser);

        Task createdTask = taskService.createTask(existingTask);

        Task updateRequest = new Task();
        updateRequest.setTitle("Updated title");
        updateRequest.setDescription("Updated description");
        updateRequest.setStatus(TaskStatus.DONE);
        updateRequest.setPriority(TaskPriority.HIGH);
        updateRequest.setDueDate(LocalDate.of(2026, 9, 15));

        Task updatedTask = taskService.updateTask(
                createdTask.getId(),
                updateRequest,
                testUser
        );

        assertThat(updatedTask.getTitle()).isEqualTo("Updated title");
        assertThat(updatedTask.getDescription())
                .isEqualTo("Updated description");
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(updatedTask.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(updatedTask.getDueDate())
                .isEqualTo(LocalDate.of(2026, 9, 15));
        assertThat(updatedTask.getUser().getId()).isEqualTo(testUser.getId());

        Task foundTask = taskService.getTaskById(
                createdTask.getId(),
                testUser
        );

        assertThat(foundTask.getTitle()).isEqualTo("Updated title");
        assertThat(foundTask.getDescription())
                .isEqualTo("Updated description");
        assertThat(foundTask.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(foundTask.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(foundTask.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldNotUpdateTaskOfAnotherUser() {
        Task existingTask = new Task();
        existingTask.setTitle("Private task");
        existingTask.setUser(testUser);

        Task createdTask = taskService.createTask(existingTask);

        Task updateRequest = new Task();
        updateRequest.setTitle("Unauthorized update");
        updateRequest.setDescription("Should not be saved");
        updateRequest.setStatus(TaskStatus.DONE);
        updateRequest.setPriority(TaskPriority.HIGH);

        assertThatThrownBy(() ->
                taskService.updateTask(
                        createdTask.getId(),
                        updateRequest,
                        anotherUser
                )
        )
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage(
                        "Task with id " + createdTask.getId() + " not found"
                );

        Task unchangedTask = taskService.getTaskById(
                createdTask.getId(),
                testUser
        );

        assertThat(unchangedTask.getTitle())
                .isEqualTo("Private task");
    }

    @Test
    void shouldDeleteTaskThroughService() {
        Task task = new Task();
        task.setTitle("Task to delete");
        task.setUser(testUser);

        Task createdTask = taskService.createTask(task);

        taskService.deleteTask(
                createdTask.getId(),
                testUser
        );

        assertThatThrownBy(() ->
                taskService.getTaskById(
                        createdTask.getId(),
                        testUser
                )
        )
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage(
                        "Task with id " + createdTask.getId() + " not found"
                );

        assertThat(taskRepository.existsById(createdTask.getId()))
                .isFalse();
    }

    @Test
    void shouldNotDeleteTaskOfAnotherUser() {
        Task task = new Task();
        task.setTitle("Private task");
        task.setUser(testUser);

        Task createdTask = taskService.createTask(task);

        assertThatThrownBy(() ->
                taskService.deleteTask(
                        createdTask.getId(),
                        anotherUser
                )
        )
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage(
                        "Task with id " + createdTask.getId() + " not found"
                );

        assertThat(taskRepository.existsById(createdTask.getId()))
                .isTrue();
    }

    @Test
    void shouldRejectTaskWithEmptyTitle() {
        Task task = new Task();
        task.setTitle("");
        task.setUser(testUser);

        assertThatThrownBy(() -> taskService.createTask(task))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task title must not be empty");

        assertThat(taskRepository.count()).isZero();
    }
}
