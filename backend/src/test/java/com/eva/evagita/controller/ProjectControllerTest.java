package com.eva.evagita.controller;

import com.eva.evagita.dto.project.ProjectResponse;
import com.eva.evagita.exception.GlobalExceptionHandler;
import com.eva.evagita.model.Project;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.security.JwtService;
import com.eva.evagita.security.SecurityConfig;
import com.eva.evagita.service.ProjectService;
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
import static org.mockito.Mockito.doThrow;
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

@WebMvcTest(ProjectController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

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
    void shouldCreateProject() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test project");
        project.setDescription("Test description");
        project.setUser(testUser);

        when(projectService.createProject(any(Project.class)))
                .thenReturn(project);

        mockMvc.perform(post("/api/projects")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test project",
                                  "description": "Test description"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test project"))
                .andExpect(jsonPath("$.description").value("Test description"));

        verify(projectService).createProject(any(Project.class));
    }

    @Test
    void shouldGetAllProjects() throws Exception {
        Project firstProject = new Project();
        firstProject.setId(1L);
        firstProject.setName("First project");
        firstProject.setDescription("First description");
        firstProject.setUser(testUser);

        Project secondProject = new Project();
        secondProject.setId(2L);
        secondProject.setName("Second project");
        secondProject.setDescription("Second description");
        secondProject.setUser(testUser);

        when(projectService.getAllProjects(testUser))
                .thenReturn(List.of(firstProject, secondProject));

        mockMvc.perform(get("/api/projects")
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("First project"))
                .andExpect(jsonPath("$[0].description").value("First description"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Second project"))
                .andExpect(jsonPath("$[1].description").value("Second description"));

        verify(projectService).getAllProjects(testUser);
    }

    @Test
    void shouldGetProjectById() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test project");
        project.setDescription("Test description");
        project.setUser(testUser);

        when(projectService.getProjectById(1L, testUser))
                .thenReturn(project);

        mockMvc.perform(get("/api/projects/1")
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test project"))
                .andExpect(jsonPath("$.description").value("Test description"));

        verify(projectService).getProjectById(1L, testUser);
    }

    @Test
    void shouldUpdateProject() throws Exception {
        Project updatedProject = new Project();
        updatedProject.setId(1L);
        updatedProject.setName("Updated project");
        updatedProject.setDescription("Updated description");
        updatedProject.setUser(testUser);

        when(projectService.updateProject(
                eq(1L),
                any(Project.class),
                eq(testUser)
        )).thenReturn(updatedProject);

        mockMvc.perform(put("/api/projects/1")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated project",
                                  "description": "Updated description"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated project"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(projectService).updateProject(
                eq(1L),
                any(Project.class),
                eq(testUser)
        );
    }

    @Test
    void shouldDeleteProject() throws Exception {
        mockMvc.perform(delete("/api/projects/1")
                        .with(user("test-user")))
                .andExpect(status().isNoContent());

        verify(projectService).deleteProject(1L, testUser);
    }

    @Test
    void shouldReturn400WhenProjectNameIsEmpty() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "Test description"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Project name must not be blank"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(projectService, never()).createProject(any(Project.class));
    }

    @Test
    void shouldReturn400WhenProjectNameContainsOnlyWhitespace() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   ",
                                  "description": "Test description"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Project name must not be blank"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(projectService, never()).createProject(any(Project.class));
    }

    @Test
    void shouldReturn400WhenProjectNameIsNull() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Test description"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Project name must not be blank"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(projectService, never()).createProject(any(Project.class));
    }

    @Test
    void shouldReturn400WhenProjectNameIsTooLong() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901",
                                  "description": "Test description"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Project name must not exceed 100 characters"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(projectService, never()).createProject(any(Project.class));
    }

    @Test
    void shouldReturn400WhenProjectDescriptionIsTooLong() throws Exception {
        String description = "a".repeat(1001);

        mockMvc.perform(post("/api/projects")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test project",
                                  "description": "%s"
                                }
                                """.formatted(description)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Project description must not exceed 1000 characters"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(projectService, never()).createProject(any(Project.class));
    }


    @Test
    void shouldReturn404WhenAccessingAnotherUsersProject() throws Exception {
        when(projectService.getProjectById(1L, testUser))
                .thenThrow(new com.eva.evagita.exception.ProjectNotFoundException(
                        "Project not found with id: 1"
                ));

        mockMvc.perform(get("/api/projects/1")
                        .with(user("test-user")))
                .andExpect(status().isNotFound());

        verify(projectService).getProjectById(1L, testUser);
    }

    @Test
    void shouldReturn404WhenUpdatingAnotherUsersProject() throws Exception {
        when(projectService.updateProject(
                eq(1L),
                any(Project.class),
                eq(testUser)
        )).thenThrow(new com.eva.evagita.exception.ProjectNotFoundException(
                "Project not found with id: 1"
        ));

        mockMvc.perform(put("/api/projects/1")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Hacked project",
                                  "description": "Hacked description"
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(projectService).updateProject(
                eq(1L),
                any(Project.class),
                eq(testUser)
        );
    }

    @Test
    void shouldReturn404WhenDeletingAnotherUsersProject() throws Exception {
        doThrow(new com.eva.evagita.exception.ProjectNotFoundException(
                "Project not found with id: 1"
        )).when(projectService).deleteProject(1L, testUser);

        mockMvc.perform(delete("/api/projects/1")
                        .with(user("test-user")))
                .andExpect(status().isNotFound());

        verify(projectService).deleteProject(1L, testUser);
    }

    @Test
    void shouldReturn401WhenAccessingProjectWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isUnauthorized());

        verify(projectService, never())
                .getProjectById(any(Long.class), any(User.class));
    }

}
