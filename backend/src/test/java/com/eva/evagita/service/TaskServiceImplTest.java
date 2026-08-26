package com.eva.evagita.service;

import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;

import java.time.LocalDate;

import com.eva.evagita.model.Task;
import com.eva.evagita.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRepository);
    }

    @Test
    void createTask_shouldSaveAndReturnTask() {
        Task task = new Task();
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.createTask(task);

        assertSame(task, result);
        verify(taskRepository).save(task);
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        Task task1 = new Task();
        Task task2 = new Task();

        List<Task> tasks = List.of(task1, task2);

        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        assertEquals(tasks, result);
        verify(taskRepository).findAll();
    }

    @Test
    void getTaskById_shouldReturnTaskWhenExists() {
        Long id = 1L;
        Task task = new Task();

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(id);

        assertSame(task, result);
        verify(taskRepository).findById(id);
    }

    @Test
    void getTaskById_shouldThrowExceptionWhenNotFound() {
        Long id = 999L;

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.getTaskById(id)
        );

        assertEquals("Task not found: " + id, exception.getMessage());
        verify(taskRepository).findById(id);
    }

    @Test
    void deleteTask_shouldFindAndDeleteTask() {
        Long id = 1L;
        Task task = new Task();

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        taskService.deleteTask(id);

        verify(taskRepository).findById(id);
        verify(taskRepository).delete(task);
    }

    @Test
    void updateTask_shouldUpdateExistingTaskAndSaveIt() {
    Long id = 1L;

    Task existingTask = new Task();
    existingTask.setTitle("Old title");
    existingTask.setDescription("Old description");
    existingTask.setStatus(TaskStatus.TODO);
    existingTask.setPriority(TaskPriority.LOW);
    existingTask.setDueDate(LocalDate.of(2026, 8, 30));

    Task updatedTask = new Task();
    updatedTask.setTitle("Updated title");
    updatedTask.setDescription("Updated description");
    updatedTask.setStatus(TaskStatus.DONE);
    updatedTask.setPriority(TaskPriority.HIGH);
    updatedTask.setDueDate(LocalDate.of(2026, 9, 15));

    when(taskRepository.findById(id)).thenReturn(Optional.of(existingTask));
    when(taskRepository.save(existingTask)).thenReturn(existingTask);

    Task result = taskService.updateTask(id, updatedTask);

    assertEquals("Updated title", existingTask.getTitle());
    assertEquals("Updated description", existingTask.getDescription());
    assertEquals(TaskStatus.DONE, existingTask.getStatus());
    assertEquals(TaskPriority.HIGH, existingTask.getPriority());
    assertEquals(LocalDate.of(2026, 9, 15), existingTask.getDueDate());

    assertSame(existingTask, result);

    verify(taskRepository).findById(id);
    verify(taskRepository).save(existingTask);
    }
}
