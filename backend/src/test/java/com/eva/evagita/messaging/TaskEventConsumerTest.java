package com.eva.evagita.messaging;

import com.eva.evagita.event.TaskEvent;
import com.eva.evagita.model.Notification;
import com.eva.evagita.model.NotificationType;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    private TaskEventConsumer taskEventConsumer;

    @BeforeEach
    void setUp() {
        taskEventConsumer = new TaskEventConsumer(
                notificationService,
                userRepository
        );
    }

    @Test
    void consume_shouldCreateNotificationForTaskCreatedEvent() {
        User user = new User(
                "consumer-test-user",
                "consumer-test@example.com",
                "password"
        );

        TaskEvent event = new TaskEvent(
                NotificationType.TASK_CREATED,
                1L,
                10L,
                "Test task"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        taskEventConsumer.consume(event);

        verify(notificationService).createNotification(
                user,
                NotificationType.TASK_CREATED,
                "Task created: Test task"
        );
    }

    @Test
    void consume_shouldCreateNotificationForTaskUpdatedEvent() {
        User user = new User(
                "consumer-test-user",
                "consumer-test@example.com",
                "password"
        );

        TaskEvent event = new TaskEvent(
                NotificationType.TASK_UPDATED,
                1L,
                10L,
                "Updated task"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        taskEventConsumer.consume(event);

        verify(notificationService).createNotification(
                user,
                NotificationType.TASK_UPDATED,
                "Task updated: Updated task"
        );
    }

    @Test
    void consume_shouldCreateNotificationForTaskCompletedEvent() {
        User user = new User(
                "consumer-test-user",
                "consumer-test@example.com",
                "password"
        );

        TaskEvent event = new TaskEvent(
                NotificationType.TASK_COMPLETED,
                1L,
                10L,
                "Completed task"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        taskEventConsumer.consume(event);

        verify(notificationService).createNotification(
                user,
                NotificationType.TASK_COMPLETED,
                "Task completed: Completed task"
        );
    }

    @Test
    void consume_shouldCreateNotificationForTaskOverdueEvent() {
        User user = new User(
                "consumer-test-user",
                "consumer-test@example.com",
                "password"
        );

        TaskEvent event = new TaskEvent(
                NotificationType.TASK_OVERDUE,
                1L,
                10L,
                "Overdue task"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        taskEventConsumer.consume(event);

        verify(notificationService).createNotification(
                user,
                NotificationType.TASK_OVERDUE,
                "Task overdue: Overdue task"
        );
    }

    @Test
    void consume_shouldRejectEventWhenUserDoesNotExist() {
        TaskEvent event = new TaskEvent(
                NotificationType.TASK_CREATED,
                999L,
                10L,
                "Unknown user task"
        );

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> taskEventConsumer.consume(event)
        );

        verify(notificationService, never())
                .createNotification(any(), any(), any());
    }

    @Test
    void consume_shouldPassCorrectNotificationData() {
        User user = new User(
                "consumer-test-user",
                "consumer-test@example.com",
                "password"
        );

        TaskEvent event = new TaskEvent(
                NotificationType.TASK_CREATED,
                1L,
                42L,
                "Important task"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        taskEventConsumer.consume(event);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<NotificationType> typeCaptor =
                ArgumentCaptor.forClass(NotificationType.class);
        ArgumentCaptor<String> messageCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(notificationService).createNotification(
                userCaptor.capture(),
                typeCaptor.capture(),
                messageCaptor.capture()
        );

        assertThat(userCaptor.getValue()).isSameAs(user);
        assertThat(typeCaptor.getValue())
                .isEqualTo(NotificationType.TASK_CREATED);
        assertThat(messageCaptor.getValue())
                .isEqualTo("Task created: Important task");
    }
}
