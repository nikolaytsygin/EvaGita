package com.eva.evagita.service;

import com.eva.evagita.model.Notification;
import com.eva.evagita.model.NotificationType;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private User testUser;

    @Mock
    private User anotherUser;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository);
    }

    @Test
    void getNotifications_shouldReturnUserNotifications() {
        Notification notification = createNotification(
                testUser,
                NotificationType.TASK_CREATED,
                "Task created"
        );

        when(notificationRepository.findAllByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(List.of(notification));

        List<Notification> result =
                notificationService.getNotifications(testUser);

        assertThat(result).containsExactly(notification);

        verify(notificationRepository)
                .findAllByUserOrderByCreatedAtDesc(testUser);
    }

    @Test
    void getUnreadNotifications_shouldReturnUnreadNotifications() {
        Notification notification = createNotification(
                testUser,
                NotificationType.TASK_UPDATED,
                "Task updated"
        );

        when(notificationRepository
                .findAllByUserAndReadFalseOrderByCreatedAtDesc(testUser))
                .thenReturn(List.of(notification));

        List<Notification> result =
                notificationService.getUnreadNotifications(testUser);

        assertThat(result).containsExactly(notification);

        verify(notificationRepository)
                .findAllByUserAndReadFalseOrderByCreatedAtDesc(testUser);
    }

    @Test
    void countUnreadNotifications_shouldReturnRepositoryCount() {
        when(notificationRepository.countByUserAndReadFalse(testUser))
                .thenReturn(3L);

        long result =
                notificationService.countUnreadNotifications(testUser);

        assertThat(result).isEqualTo(3L);

        verify(notificationRepository)
                .countByUserAndReadFalse(testUser);
    }

    @Test
    void createNotification_shouldSaveNotification() {
        Notification savedNotification = createNotification(
                testUser,
                NotificationType.TASK_COMPLETED,
                "Task completed"
        );

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(savedNotification);

        Notification result = notificationService.createNotification(
                testUser,
                NotificationType.TASK_COMPLETED,
                "Task completed"
        );

        assertThat(result).isSameAs(savedNotification);

        verify(notificationRepository).save(argThat(notification ->
                notification.getUser() == testUser
                        && notification.getType() == NotificationType.TASK_COMPLETED
                        && notification.getMessage().equals("Task completed")
                        && !notification.isRead()
        ));
    }

    @Test
    void markAsRead_shouldMarkOwnNotificationAsRead() {
        when(testUser.getId()).thenReturn(1L);

        Notification notification = createNotification(
                testUser,
                NotificationType.TASK_CREATED,
                "Task created"
        );

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));

        when(notificationRepository.save(notification))
                .thenReturn(notification);

        Notification result =
                notificationService.markAsRead(1L, testUser);

        assertThat(result).isSameAs(notification);
        assertThat(result.isRead()).isTrue();

        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_shouldRejectNotificationOfAnotherUser() {
        when(testUser.getId()).thenReturn(1L);
        when(anotherUser.getId()).thenReturn(2L);

        Notification notification = createNotification(
                anotherUser,
                NotificationType.TASK_CREATED,
                "Task created"
        );

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));

        assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.markAsRead(1L, testUser)
        );

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_shouldRejectMissingNotification() {
        when(notificationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.markAsRead(999L, testUser)
        );

        verify(notificationRepository, never()).save(any());
    }

    private Notification createNotification(
            User user,
            NotificationType type,
            String message
    ) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        return notification;
    }
}
