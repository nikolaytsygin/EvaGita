package com.eva.evagita.controller;

import com.eva.evagita.dto.TaskResponse;
import com.eva.evagita.exception.GlobalExceptionHandler;
import com.eva.evagita.exception.TaskNotFoundException;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.service.TaskService;
import com.eva.evagita.security.JwtService;
import com.eva.evagita.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "test-user",
                "test@example.com",
                "password"
        );

        when(userRepository.findByUsername("test-user"))
                .thenReturn(Optional.of(testUser));
    }

    @Test
    void shouldGetAllTasks() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test task");
        task.setDescription("Test description");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setUser(testUser);

        when(taskService.getAllTasks(testUser))
                .thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks")
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test task"))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"));

        verify(taskService).getAllTasks(testUser);
    }

    @Test
    void shouldGetTaskById() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test task");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setUser(testUser);

        when(taskService.getTaskById(1L, testUser))
                .thenReturn(task);

        mockMvc.perform(get("/api/tasks/1")
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test task"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        verify(taskService).getTaskById(1L, testUser);
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        Long id = 999L;

        when(taskService.getTaskById(id, testUser))
                .thenThrow(new TaskNotFoundException(id));

        mockMvc.perform(get("/api/tasks/" + id)
                        .with(user("test-user")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Task with id 999 not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldCreateTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("New task");
        task.setDescription("New description");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setUser(testUser);

        when(taskService.createTask(any(Task.class)))
                .thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "New task",
                                  "description": "New description",
                                  "status": "TODO",
                                  "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New task"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        verify(userRepository).findByUsername("test-user");
        verify(taskService).createTask(any(Task.class));
    }

    @Test
    void shouldReturn400WhenTaskTitleIsEmpty() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "Test description",
                                  "status": "TODO",
                                  "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Task title must not be empty"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(taskService, never()).createTask(any(Task.class));
    }

    @Test
    void shouldReturn400WhenTaskTitleContainsOnlyWhitespace() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "Test description",
                                  "status": "TODO",
                                  "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Task title must not be empty"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(taskService, never()).createTask(any(Task.class));
    }

    @Test
    void shouldReturn400WhenTaskTitleIsNull() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Test description",
                                  "status": "TODO",
                                  "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Task title must not be empty"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(taskService, never()).createTask(any(Task.class));
    }

    @Test
    void shouldReturn500WhenUnexpectedExceptionOccurs() throws Exception {
        when(taskService.getTaskById(1L, testUser))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/tasks/1")
                        .with(user("test-user")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldUpdateTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Updated task");
        task.setDescription("Updated description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.HIGH);
        task.setUser(testUser);

        when(taskService.updateTask(
                eq(1L),
                any(Task.class),
                eq(testUser)
        )).thenReturn(task);

        mockMvc.perform(put("/api/tasks/1")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated task",
                                  "description": "Updated description",
                                  "status": "IN_PROGRESS",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        verify(taskService).updateTask(
                eq(1L),
                any(Task.class),
                eq(testUser)
        );
    }

    @Test
    void shouldDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTask(1L, testUser);

        mockMvc.perform(delete("/api/tasks/1")
                        .with(user("test-user")))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L, testUser);
    }
}
