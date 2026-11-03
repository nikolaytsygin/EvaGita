package com.eva.evagita.security;

import com.eva.evagita.PostgresIntegrationTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.repository.NotificationRepository;
import com.eva.evagita.repository.ProjectRepository;
import com.eva.evagita.repository.TagRepository;
import com.eva.evagita.repository.TaskRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class JwtAuthenticationIntegrationTest extends PostgresIntegrationTest {

    private static final String SECRET =
            "EvaGitaJwtSecretKeyForDevelopmentOnly_ChangeThisToAStrongSecretKey_2026";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtService jwtService;

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

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
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
        testUser = userRepository.save(new User(
                "header-test-user",
                "header-test@example.com",
                "test-password"
        ));
    }

    @Test
    void shouldReturn401ForExpiredJwt() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        Date now = new Date();

        String expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(now.getTime() - 2000))
                .expiration(new Date(now.getTime() - 1000))
                .signWith(key)
                .compact();

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForInvalidJwt() throws Exception {
        String invalidToken = "this.is.not.a.valid.jwt";

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAuthenticateAuthorizationBearerHeader() throws Exception {
        String token = jwtService.generateToken(testUser);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectLegacyCustomAuthorizationHeader() throws Exception {
        String token = jwtService.generateToken(testUser);

        mockMvc.perform(get("/api/users/me")
                        .header("X-EvaGita-Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnknownRouteByDefault() throws Exception {
        mockMvc.perform(get("/unknown-route"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldExposeOnlyHealthActuatorEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized());
    }
}
