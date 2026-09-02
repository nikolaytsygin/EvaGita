package com.eva.evagita.controller;

import com.eva.evagita.dto.NotificationResponse;
import com.eva.evagita.exception.GlobalExceptionHandler;
import com.eva.evagita.model.Notification;
import com.eva.evagita.model.NotificationType;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.security.JwtService;
import com.eva.evagita.security.SecurityConfig;
import com.eva.evagita.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

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
    void shouldGetNotifications() throws Exception {
        Notification notification = createNotification(
                NotificationType.TASK_CREATED,
                "Task created"
        );

        when(notificationService.getNotifications(testUser))
                .thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications")
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TASK_CREATED"))
                .andExpect(jsonPath("$[0].message").value("Task created"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists());

        verify(userRepository).findByUsername("test-user");
        verify(notificationService).getNotifications(testUser);
    }

    @Test
    void shouldGetUnreadNotifications() throws Exception {
        Notification notification = createNotification(
                NotificationType.TASK_UPDATED,
                "Task updated"
        );

        when(notificationService.getUnreadNotifications(testUser))
                .thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications/unread")
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TASK_UPDATED"))
                .andExpect(jsonPath("$[0].message").value("Task updated"))
                .andExpect(jsonPath("$[0].read").value(false));

        verify(userRepository).findByUsername("test-user");
        verify(notificationService).getUnreadNotifications(testUser);
    }

    @Test
    void shouldCountUnreadNotifications() throws Exception {
        when(notificationService.countUnreadNotifications(testUser))
                .thenReturn(3L);

        mockMvc.perform(get("/api/notifications/unread/count")
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));

        verify(userRepository).findByUsername("test-user");
        verify(notificationService).countUnreadNotifications(testUser);
    }

    @Test
    void shouldMarkNotificationAsRead() throws Exception {
        Notification notification = createNotification(
                NotificationType.TASK_CREATED,
                "Task created"
        );
        when(notification.isRead()).thenReturn(true);

        when(notificationService.markAsRead(eq(1L), eq(testUser)))
                .thenReturn(notification);

        mockMvc.perform(patch("/api/notifications/1/read")
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TASK_CREATED"))
                .andExpect(jsonPath("$.message").value("Task created"))
                .andExpect(jsonPath("$.read").value(true));

        verify(userRepository).findByUsername("test-user");
        verify(notificationService).markAsRead(1L, testUser);
    }

    private Notification createNotification(
            NotificationType type,
            String message
    ) {
        Notification notification = mock(Notification.class);
        when(notification.getType()).thenReturn(type);
        when(notification.getMessage()).thenReturn(message);
        when(notification.isRead()).thenReturn(false);
        when(notification.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 9, 2, 12, 0));
        when(notification.getUpdatedAt()).thenReturn(LocalDateTime.of(2026, 9, 2, 12, 0));
        return notification;
    }
}
