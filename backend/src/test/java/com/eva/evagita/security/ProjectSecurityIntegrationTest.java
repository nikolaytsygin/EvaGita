package com.eva.evagita.security;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.model.Project;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.NotificationRepository;
import com.eva.evagita.repository.ProjectRepository;
import com.eva.evagita.repository.TagRepository;
import com.eva.evagita.repository.TaskRepository;
import com.eva.evagita.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ProjectSecurityIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private MockMvc mockMvc;
    private User testUser;
    private User anotherUser;
    private String testUserToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        taskRepository.deleteAll();
        projectRepository.deleteAll();
        tagRepository.deleteAll();
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(new User(
                "project-security-user",
                "project-security@example.com",
                "password"
        ));

        anotherUser = userRepository.save(new User(
                "another-project-security-user",
                "another-project-security@example.com",
                "password"
        ));

        testUserToken = jwtService.generateToken(testUser.getUsername());
    }

    @Test
    void getAnotherUsersProject_shouldReturnNotFound() throws Exception {
        Project foreignProject = createProject(
                "Private project",
                "Private description",
                anotherUser
        );

        mockMvc.perform(get("/api/projects/{id}", foreignProject.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNotFound());

        assertThat(projectRepository.findById(foreignProject.getId()))
                .isPresent()
                .get()
                .extracting(Project::getName)
                .isEqualTo("Private project");
    }

    @Test
    void updateAnotherUsersProject_shouldReturnNotFoundAndKeepOriginalData()
            throws Exception {
        Project foreignProject = createProject(
                "Original project",
                "Original description",
                anotherUser
        );
        Long foreignProjectId = foreignProject.getId();

        mockMvc.perform(put("/api/projects/{id}", foreignProjectId)
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Hacked project",
                                  "description": "Hacked description"
                                }
                                """))
                .andExpect(status().isNotFound());

        assertThat(projectRepository.findById(foreignProjectId))
                .isPresent()
                .get()
                .satisfies(project -> {
                    assertThat(project.getName()).isEqualTo("Original project");
                    assertThat(project.getDescription())
                            .isEqualTo("Original description");
                    assertThat(project.getUser().getId())
                            .isEqualTo(anotherUser.getId());
                });
    }

    @Test
    void deleteAnotherUsersProject_shouldReturnNotFoundAndKeepProject()
            throws Exception {
        Project foreignProject = createProject(
                "Project to protect",
                "Must not be deleted",
                anotherUser
        );
        Long foreignProjectId = foreignProject.getId();

        mockMvc.perform(delete("/api/projects/{id}", foreignProjectId)
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNotFound());

        assertThat(projectRepository.findById(foreignProjectId))
                .isPresent()
                .get()
                .extracting(Project::getName)
                .isEqualTo("Project to protect");
    }

    @Test
    void getAllProjects_shouldReturnOnlyCurrentUsersProjects() throws Exception {
        createProject("My project", "My description", testUser);
        createProject("Foreign project", "Foreign description", anotherUser);

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("My project"));
    }

    private Project createProject(String name, String description, User owner) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setUser(owner);
        return projectRepository.saveAndFlush(project);
    }
}
