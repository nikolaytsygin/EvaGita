package com.eva.evagita.security;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.model.Notification;
import com.eva.evagita.model.NotificationType;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.NotificationRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class NotificationSecurityIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TaskRepository taskRepository;

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

        notificationRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.saveAndFlush(new User(
                "notification-security-user",
                "notification-security@example.com",
                "password"
        ));

        anotherUser = userRepository.saveAndFlush(new User(
                "another-notification-security-user",
                "another-notification-security@example.com",
                "password"
        ));

        testUserToken = jwtService.generateToken(testUser.getUsername());
    }

    @Test
    void getNotifications_shouldReturnOnlyCurrentUsersNotifications()
            throws Exception {
        createNotification(testUser, "My private notification");
        createNotification(anotherUser, "Foreign private notification");

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].message")
                        .value("My private notification"));
    }

    @Test
    void getUnreadNotifications_shouldReturnOnlyCurrentUsersUnreadNotifications()
            throws Exception {
        createNotification(testUser, "My unread notification");
        createNotification(anotherUser, "Foreign unread notification");

        mockMvc.perform(get("/api/notifications/unread")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].message")
                        .value("My unread notification"))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void countUnreadNotifications_shouldCountOnlyCurrentUsersNotifications()
            throws Exception {
        createNotification(testUser, "My unread notification");
        createNotification(testUser, "My second unread notification");
        createNotification(anotherUser, "Foreign unread notification");

        mockMvc.perform(get("/api/notifications/unread/count")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));
    }

    @Test
    void markAnotherUsersNotificationAsRead_shouldNotChangeNotification()
            throws Exception {
        Notification foreignNotification = createNotification(
                anotherUser,
                "Notification that must remain unread"
        );
        Long notificationId = foreignNotification.getId();

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId)
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertThat(notificationRepository.findById(notificationId))
                .isPresent()
                .get()
                .satisfies(notification -> {
                    assertThat(notification.isRead()).isFalse();
                    assertThat(notification.getMessage())
                            .isEqualTo("Notification that must remain unread");
                    assertThat(notification.getUser().getId())
                            .isEqualTo(anotherUser.getId());
                });
    }

    private Notification createNotification(User owner, String message) {
        Notification notification = new Notification();
        notification.setUser(owner);
        notification.setType(NotificationType.TASK_UPDATED);
        notification.setMessage(message);
        notification.setRead(false);
        return notificationRepository.saveAndFlush(notification);
    }
}
