package com.eva.evagita.service;

import com.eva.evagita.exception.TaskNotFoundException;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskServiceImpl taskService;

    private User testUser;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRepository);

        testUser = new User(
                "test-user",
                "test@example.com",
                "password"
        );
    }

    @Test
    void createTask_shouldSaveAndReturnTask() {
        Task task = new Task();
        task.setTitle("Test task");
        task.setUser(testUser);

        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.createTask(task);

        assertSame(task, result);
        verify(taskRepository).save(task);
    }

    @Test
    void getAllTasks_shouldReturnTasksForUser() {
        Task task1 = new Task();
        Task task2 = new Task();

        List<Task> tasks = List.of(task1, task2);

        when(taskRepository.findAllByUser(testUser)).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks(testUser);

        assertEquals(2, result.size());
        assertEquals(tasks, result);
        verify(taskRepository).findAllByUser(testUser);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void getTaskById_shouldReturnTaskWhenExistsForUser() {
        Long id = 1L;
        Task task = new Task();
        task.setUser(testUser);

        when(taskRepository.findByIdAndUser(id, testUser))
                .thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(id, testUser);

        assertSame(task, result);
        verify(taskRepository).findByIdAndUser(id, testUser);
        verify(taskRepository, never()).findById(id);
    }

    @Test
    void getTaskById_shouldThrowTaskNotFoundExceptionWhenTaskDoesNotBelongToUser() {
        Long id = 999L;

        when(taskRepository.findByIdAndUser(id, testUser))
                .thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTaskById(id, testUser)
        );

        assertEquals(
                "Task with id " + id + " not found",
                exception.getMessage()
        );

        verify(taskRepository).findByIdAndUser(id, testUser);
    }

    @Test
    void createTask_shouldThrowExceptionWhenTitleIsEmpty() {
        Task task = new Task();
        task.setTitle("");
        task.setUser(testUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(task)
        );

        assertEquals(
                "Task title must not be empty",
                exception.getMessage()
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_shouldFindAndDeleteTaskForUser() {
        Long id = 1L;
        Task task = new Task();
        task.setUser(testUser);

        when(taskRepository.findByIdAndUser(id, testUser))
                .thenReturn(Optional.of(task));

        taskService.deleteTask(id, testUser);

        verify(taskRepository).findByIdAndUser(id, testUser);
        verify(taskRepository).delete(task);
        verify(taskRepository, never()).findById(id);
    }

    @Test
    void updateTask_shouldUpdateExistingTaskForUserAndSaveIt() {
        Long id = 1L;

        Task existingTask = new Task();
        existingTask.setTitle("Old title");
        existingTask.setDescription("Old description");
        existingTask.setStatus(TaskStatus.TODO);
        existingTask.setPriority(TaskPriority.LOW);
        existingTask.setDueDate(LocalDate.of(2026, 8, 30));
        existingTask.setUser(testUser);

        Task updatedTask = new Task();
        updatedTask.setTitle("Updated title");
        updatedTask.setDescription("Updated description");
        updatedTask.setStatus(TaskStatus.DONE);
        updatedTask.setPriority(TaskPriority.HIGH);
        updatedTask.setDueDate(LocalDate.of(2026, 9, 15));

        when(taskRepository.findByIdAndUser(id, testUser))
                .thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask))
                .thenReturn(existingTask);

        Task result = taskService.updateTask(
                id,
                updatedTask,
                testUser
        );

        assertEquals("Updated title", existingTask.getTitle());
        assertEquals("Updated description", existingTask.getDescription());
        assertEquals(TaskStatus.DONE, existingTask.getStatus());
        assertEquals(TaskPriority.HIGH, existingTask.getPriority());
        assertEquals(
                LocalDate.of(2026, 9, 15),
                existingTask.getDueDate()
        );

        assertSame(existingTask, result);

        verify(taskRepository).findByIdAndUser(id, testUser);
        verify(taskRepository).save(existingTask);
        verify(taskRepository, never()).findById(id);
    }

    @Test
    void updateTask_shouldThrowWhenTaskDoesNotBelongToUser() {
        Long id = 1L;

        Task updatedTask = new Task();
        updatedTask.setTitle("Unauthorized update");
        updatedTask.setStatus(TaskStatus.DONE);
        updatedTask.setPriority(TaskPriority.HIGH);

        when(taskRepository.findByIdAndUser(id, testUser))
                .thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.updateTask(
                        id,
                        updatedTask,
                        testUser
                )
        );

        assertEquals(
                "Task with id " + id + " not found",
                exception.getMessage()
        );

        verify(taskRepository).findByIdAndUser(id, testUser);
        verify(taskRepository, never()).save(any(Task.class));
    }
}
