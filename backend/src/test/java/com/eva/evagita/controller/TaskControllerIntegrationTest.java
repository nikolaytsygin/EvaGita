package com.eva.evagita.controller;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.model.Project;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.ProjectRepository;
import com.eva.evagita.repository.TaskRepository;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.security.JwtService;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class TaskControllerIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private MockMvc mockMvc;

    private User testUser;

    private User anotherUser;

    private String testUserToken;

    private String anotherUserToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(
                "integration-test-user",
                "integration-test@example.com",
                "password"
        );

        anotherUser = new User(
                "another-integration-user",
                "another-integration@example.com",
                "password"
        );

        testUser = userRepository.save(testUser);
        anotherUser = userRepository.save(anotherUser);

        testUserToken = jwtService.generateToken(testUser.getUsername());
        anotherUserToken = jwtService.generateToken(anotherUser.getUsername());
    }

    @Test
    void shouldCreateTaskThroughRestApi() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + testUserToken)
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
                .andExpect(jsonPath("$.title")
                        .value("REST integration task"))
                .andExpect(jsonPath("$.description")
                        .value("Created through REST API"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        assertThat(taskRepository.count()).isEqualTo(1);

        Task createdTask = taskRepository.findAll().getFirst();

        assertThat(createdTask.getUser().getId())
                .isEqualTo(testUser.getId());
    }

    @Test
    void shouldCreateTaskWithProjectThroughRestApi() throws Exception {
        Project project = createProject(
                "Integration project",
                testUser
        );

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Task with project",
                                  "description": "Task belongs to project",
                                  "status": "TODO",
                                  "priority": "HIGH",
                                  "projectId": %d
                                }
                                """.formatted(project.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title")
                        .value("Task with project"))
                .andExpect(jsonPath("$.projectId")
                        .value(project.getId().intValue()));

        Task createdTask = taskRepository.findAll().getFirst();

        assertThat(createdTask.getProject()).isNotNull();
        assertThat(createdTask.getProject().getId())
                .isEqualTo(project.getId());
    }

    @Test
    void shouldCreateTaskWithoutProjectThroughRestApi() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Task without project",
                                  "description": "Independent task",
                                  "status": "TODO",
                                  "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title")
                        .value("Task without project"))
                .andExpect(jsonPath("$.projectId").doesNotExist());

        Task createdTask = taskRepository.findAll().getFirst();

        assertThat(createdTask.getProject()).isNull();
    }

    @Test
    void shouldUpdateTaskProjectThroughRestApi() throws Exception {
        Project firstProject = createProject(
                "First project",
                testUser
        );

        Project secondProject = createProject(
                "Second project",
                testUser
        );

        Task task = createTask(
                "Task with project",
                testUser
        );
        task.setProject(firstProject);
        task = taskRepository.saveAndFlush(task);

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Task with updated project",
                                  "status": "TODO",
                                  "priority": "MEDIUM",
                                  "projectId": %d
                                }
                                """.formatted(secondProject.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId")
                        .value(secondProject.getId().intValue()));

        Task updatedTask = taskRepository.findById(task.getId())
                .orElseThrow();

        assertThat(updatedTask.getProject()).isNotNull();
        assertThat(updatedTask.getProject().getId())
                .isEqualTo(secondProject.getId());
    }

    @Test
    void shouldNotAttachTaskToAnotherUsersProjectThroughRestApi() throws Exception {
        Project anotherUsersProject = createProject(
                "Private project",
                anotherUser
        );

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Task with foreign project",
                                  "status": "TODO",
                                  "priority": "MEDIUM",
                                  "projectId": %d
                                }
                                """.formatted(anotherUsersProject.getId())))
                .andExpect(status().isBadRequest());

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void shouldGetOnlyCurrentUsersTasksThroughRestApi() throws Exception {
        Task firstTask = createTask(
                "First user task",
                testUser
        );

        Task secondTask = createTask(
                "Second user task",
                testUser
        );

        createTask(
                "Another user task",
                anotherUser
        );

        mockMvc.perform(get("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + testUserToken
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].id")
                        .value(org.hamcrest.Matchers.containsInAnyOrder(
                                firstTask.getId().intValue(),
                                secondTask.getId().intValue()
                        )))
                .andExpect(jsonPath("$[*].title")
                        .value(org.hamcrest.Matchers.containsInAnyOrder(
                                "First user task",
                                "Second user task"
                        )));
    }

    @Test
    void shouldGetOwnTaskThroughRestApi() throws Exception {
        Task task = createTask(
                "Task for GET",
                testUser
        );

        mockMvc.perform(get("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("Task for GET"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    void shouldReturn404WhenGettingAnotherUsersTask() throws Exception {
        Task task = createTask(
                "Private task",
                anotherUser
        );

        mockMvc.perform(get("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Task with id " + task.getId() + " not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldUpdateOwnTaskThroughRestApi() throws Exception {
        Task task = createTask(
                "Original title",
                testUser
        );

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + testUserToken)
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
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title")
                        .value("Updated through REST"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        Task updatedTask = taskRepository.findById(task.getId())
                .orElseThrow();

        assertThat(updatedTask.getTitle())
                .isEqualTo("Updated through REST");
        assertThat(updatedTask.getUser().getId())
                .isEqualTo(testUser.getId());
    }

    @Test
    void shouldReturn404WhenUpdatingAnotherUsersTask() throws Exception {
        Task task = createTask(
                "Private task",
                anotherUser
        );

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unauthorized update",
                                  "description": "Should not be saved",
                                  "status": "DONE",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isNotFound());

        Task unchangedTask = taskRepository.findById(task.getId())
                .orElseThrow();

        assertThat(unchangedTask.getTitle())
                .isEqualTo("Private task");
        assertThat(unchangedTask.getUser().getId())
                .isEqualTo(anotherUser.getId());
    }

    @Test
    void shouldDeleteOwnTaskThroughRestApi() throws Exception {
        Task task = createTask(
                "Task for DELETE",
                testUser
        );

        mockMvc.perform(delete("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(task.getId()))
                .isFalse();
    }

    @Test
    void shouldReturn404WhenDeletingAnotherUsersTask() throws Exception {
        Task task = createTask(
                "Private task",
                anotherUser
        );

        mockMvc.perform(delete("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNotFound());

        assertThat(taskRepository.existsById(task.getId()))
                .isTrue();
    }

    @Test
    void shouldReturn404WhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/tasks/999999")
                        .header("Authorization", "Bearer " + testUserToken))
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
                        .header("Authorization", "Bearer " + testUserToken)
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

    private Project createProject(String name, User user) {
        Project project = new Project();
        project.setName(name);
        project.setUser(user);

        return projectRepository.saveAndFlush(project);
    }

    private Task createTask(String title, User user) {
        Task task = new Task();
        task.setTitle(title);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setUser(user);

        return taskRepository.saveAndFlush(task);
    }
}
