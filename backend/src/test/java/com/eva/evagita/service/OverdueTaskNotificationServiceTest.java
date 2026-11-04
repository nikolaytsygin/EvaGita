package com.eva.evagita.service;

import com.eva.evagita.config.BusinessTimeProperties;
import com.eva.evagita.event.TaskEvent;
import com.eva.evagita.messaging.TaskEventProducer;
import com.eva.evagita.model.NotificationType;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OverdueTaskNotificationServiceTest {

    private static final Instant FIXED_INSTANT =
            Instant.parse("2026-11-03T21:15:30Z");
    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 11, 4);

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskEventProducer taskEventProducer;

    private OverdueTaskNotificationService service;

    private User testUser;

    @BeforeEach
    void setUp() {
        service = new OverdueTaskNotificationService(
                taskRepository,
                taskEventProducer,
                new BusinessClock(
                        Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC),
                        new BusinessTimeProperties()
                )
        );

        testUser = mock(User.class);
    }

    @Test
    void notifyOverdueTasks_shouldPublishOverdueEventForEachOverdueTask() {
        when(testUser.getId()).thenReturn(1L);

        Task firstTask = new Task();
        firstTask.setId(10L);
        firstTask.setTitle("Overdue task 1");
        firstTask.setDueDate(BUSINESS_DATE.minusDays(1));
        firstTask.setStatus(TaskStatus.TODO);
        firstTask.setUser(testUser);

        Task secondTask = new Task();
        secondTask.setId(20L);
        secondTask.setTitle("Overdue task 2");
        secondTask.setDueDate(BUSINESS_DATE.minusDays(3));
        secondTask.setStatus(TaskStatus.IN_PROGRESS);
        secondTask.setUser(testUser);

        when(taskRepository.findAllByDueDateBeforeAndStatusNot(
                BUSINESS_DATE,
                TaskStatus.DONE
        )).thenReturn(List.of(firstTask, secondTask));

        service.notifyOverdueTasks();

        verify(taskEventProducer).send(new TaskEvent(
                NotificationType.TASK_OVERDUE,
                1L,
                10L,
                "Overdue task 1"
        ));

        verify(taskEventProducer).send(new TaskEvent(
                NotificationType.TASK_OVERDUE,
                1L,
                20L,
                "Overdue task 2"
        ));

        verify(taskEventProducer, times(2)).send(any(TaskEvent.class));
    }

    @Test
    void notifyOverdueTasks_shouldSendEventsToEachTaskOwner() {
        User secondUser = mock(User.class);
        when(testUser.getId()).thenReturn(1L);
        when(secondUser.getId()).thenReturn(2L);

        Task firstTask = new Task();
        firstTask.setId(10L);
        firstTask.setTitle("First user's overdue task");
        firstTask.setStatus(TaskStatus.TODO);
        firstTask.setDueDate(BUSINESS_DATE.minusDays(1));
        firstTask.setUser(testUser);

        Task secondTask = new Task();
        secondTask.setId(20L);
        secondTask.setTitle("Second user's overdue task");
        secondTask.setStatus(TaskStatus.IN_PROGRESS);
        secondTask.setDueDate(BUSINESS_DATE.minusDays(2));
        secondTask.setUser(secondUser);

        when(taskRepository.findAllByDueDateBeforeAndStatusNot(
                BUSINESS_DATE,
                TaskStatus.DONE
        )).thenReturn(List.of(firstTask, secondTask));

        service.notifyOverdueTasks();

        verify(taskEventProducer).send(new TaskEvent(
                NotificationType.TASK_OVERDUE,
                1L,
                10L,
                "First user's overdue task"
        ));
        verify(taskEventProducer).send(new TaskEvent(
                NotificationType.TASK_OVERDUE,
                2L,
                20L,
                "Second user's overdue task"
        ));
        verify(taskEventProducer, times(2)).send(any(TaskEvent.class));
    }

    @Test
    void notifyOverdueTasks_shouldNotPublishEventForDoneTask() {
        when(testUser.getId()).thenReturn(1L);

        Task doneTask = new Task();
        doneTask.setId(10L);
        doneTask.setTitle("Completed task");
        doneTask.setStatus(TaskStatus.DONE);
        doneTask.setDueDate(BUSINESS_DATE.minusDays(2));
        doneTask.setUser(testUser);

        Task overdueTask = new Task();
        overdueTask.setId(20L);
        overdueTask.setTitle("Open overdue task");
        overdueTask.setStatus(TaskStatus.TODO);
        overdueTask.setDueDate(BUSINESS_DATE.minusDays(1));
        overdueTask.setUser(testUser);

        when(taskRepository.findAllByDueDateBeforeAndStatusNot(
                BUSINESS_DATE,
                TaskStatus.DONE
        )).thenReturn(List.of(overdueTask));

        service.notifyOverdueTasks();

        verify(taskEventProducer).send(new TaskEvent(
                NotificationType.TASK_OVERDUE,
                1L,
                20L,
                "Open overdue task"
        ));
        verify(taskEventProducer, times(1)).send(any(TaskEvent.class));
    }

    @Test
    void notifyOverdueTasks_shouldNotPublishAnythingWhenNoOverdueTasks() {
        when(taskRepository.findAllByDueDateBeforeAndStatusNot(
                BUSINESS_DATE,
                TaskStatus.DONE
        )).thenReturn(List.of());

        service.notifyOverdueTasks();

        verify(taskEventProducer, never()).send(any(TaskEvent.class));
    }

    @Test
    void notifyOverdueTasks_shouldNotIncludeDoneTasks() {
        when(taskRepository.findAllByDueDateBeforeAndStatusNot(
                BUSINESS_DATE,
                TaskStatus.DONE
        )).thenReturn(List.of());

        service.notifyOverdueTasks();

        verify(taskEventProducer, never()).send(any(TaskEvent.class));
    }
}
