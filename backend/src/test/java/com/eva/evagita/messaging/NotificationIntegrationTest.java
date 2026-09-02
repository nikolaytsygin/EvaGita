package com.eva.evagita.messaging;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.model.Notification;
import com.eva.evagita.model.NotificationType;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.NotificationRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class NotificationIntegrationTest extends PostgresIntegrationTest {

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

        testUser = userRepository.saveAndFlush(
                new User(
                        "notification-integration-user",
                        "notification-integration@example.com",
                        "password"
                )
        );

        testUserToken = jwtService.generateToken(testUser.getUsername());
    }

    @Test
    void shouldCreateNotificationWhenTaskIsCreatedThroughRestApi() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Notification integration task",
                                  "description": "Task for RabbitMQ integration test",
                                  "status": "TODO",
                                  "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isCreated());

        assertThat(taskRepository.count()).isEqualTo(1);

        Notification notification = waitForNotification();

        assertThat(notification.getUser().getId())
                .isEqualTo(testUser.getId());

        assertThat(notification.getType())
                .isEqualTo(NotificationType.TASK_CREATED);

        assertThat(notification.getMessage())
                .isEqualTo("Task created: Notification integration task");

        assertThat(notification.isRead())
                .isFalse();
    }

    private Notification waitForNotification() throws InterruptedException {
        for (int attempt = 0; attempt < 50; attempt++) {
            List<Notification> notifications =
                    notificationRepository.findAllByUserOrderByCreatedAtDesc(testUser);

            if (!notifications.isEmpty()) {
                return notifications.getFirst();
            }

            Thread.sleep(100);
        }

        throw new AssertionError(
                "Notification was not created by TaskEventConsumer within 5 seconds"
        );
    }
}
