package com.eva.evagita.security;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.repository.NotificationRepository;
import com.eva.evagita.repository.ProjectRepository;
import com.eva.evagita.repository.TagRepository;
import com.eva.evagita.repository.TaskRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class UserSecurityIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PasswordHashService passwordHashService;

    @Autowired
    private JwtService jwtService;

    private MockMvc mockMvc;
    private User userA;
    private User userB;
    private String userAToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        notificationRepository.deleteAll();
        notificationRepository.flush();
        taskRepository.deleteAll();
        taskRepository.flush();
        projectRepository.deleteAll();
        projectRepository.flush();
        tagRepository.deleteAll();
        tagRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();

        userA = userRepository.save(new User(
                "user-a", "user-a@example.com",
                passwordHashService.encodeIfNeeded("password-a")
        ));
        userB = userRepository.save(new User(
                "user-b", "user-b@example.com",
                passwordHashService.encodeIfNeeded("password-b")
        ));
        userAToken = jwtService.generateToken(userA.getUsername());
    }

    @Test
    void userAReceivesOwnProfileWithoutPasswordFields() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userA.getId()))
                .andExpect(jsonPath("$.username").value("user-a"))
                .andExpect(jsonPath("$.email").value("user-a@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void userACannotReadOrModifyUserBById() throws Exception {
        mockMvc.perform(get("/api/users/{id}", userB.getId())
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/users/{id}", userB.getId())
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "hijacked",
                                  "email": "hijacked@example.com",
                                  "password": "hijacked-password"
                                }
                                """))
                .andExpect(status().isNotFound());

        assertThat(userRepository.findById(userB.getId()))
                .get()
                .extracting(User::getUsername, User::getEmail)
                .containsExactly("user-b", "user-b@example.com");
    }

    @Test
    void userACannotDeleteUserB() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userB.getId())
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNotFound());

        assertThat(userRepository.findById(userB.getId())).isPresent();
    }

    @Test
    void unauthenticatedUserCannotReadOwnProfile() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationRemainsPublic() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "registered-user",
                                  "email": "registered@example.com",
                                  "password": "registration-password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
}
