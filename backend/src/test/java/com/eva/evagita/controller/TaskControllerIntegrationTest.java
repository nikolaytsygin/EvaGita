package com.eva.evagita.controller;

import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TaskRepository taskRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        taskRepository.deleteAll();
    }

    @Test
    void shouldCreateTaskThroughRestApi() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "REST integration task",
                                  "description": "Created through REST API",
                                  "status": "TODO",
                                  "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("REST integration task"))
                .andExpect(jsonPath("$.description")
                        .value("Created through REST API"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        assertThat(taskRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetTaskThroughRestApi() throws Exception {
        Task task = new Task();
        task.setTitle("Task for GET");
        task.setDescription("GET integration test");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);

        Task savedTask = taskRepository.saveAndFlush(task);

        mockMvc.perform(get("/api/tasks/" + savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Task for GET"))
                .andExpect(jsonPath("$.description")
                        .value("GET integration test"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    void shouldUpdateTaskThroughRestApi() throws Exception {
        Task task = new Task();
        task.setTitle("Original title");
        task.setDescription("Original description");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);

        Task savedTask = taskRepository.saveAndFlush(task);

        mockMvc.perform(put("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated through REST",
                                  "description": "Updated description",
                                  "status": "DONE",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title")
                        .value("Updated through REST"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        Task updatedTask = taskRepository.findById(savedTask.getId())
                .orElseThrow();

        assertThat(updatedTask.getTitle())
                .isEqualTo("Updated through REST");
        assertThat(updatedTask.getDescription())
                .isEqualTo("Updated description");
        assertThat(updatedTask.getStatus())
                .isEqualTo(TaskStatus.DONE);
        assertThat(updatedTask.getPriority())
                .isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void shouldDeleteTaskThroughRestApi() throws Exception {
        Task task = new Task();
        task.setTitle("Task for DELETE");

        Task savedTask = taskRepository.saveAndFlush(task);

        mockMvc.perform(delete("/api/tasks/" + savedTask.getId()))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(savedTask.getId()))
                .isFalse();
    }

    @Test
    void shouldReturn404WhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/tasks/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Task with id 999999 not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400WhenCreatingTaskWithEmptyTitle() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "Invalid task",
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

        assertThat(taskRepository.count()).isZero();
    }
}
