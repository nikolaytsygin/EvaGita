package com.eva.evagita.service;

import com.eva.evagita.dto.DashboardResponse;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private TaskService taskService;

    private DashboardServiceImpl dashboardService;

    private User testUser;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(taskService);

        testUser = new User(
                "test-user",
                "test@example.com",
                "password"
        );
    }

    @Test
    void getDashboard_shouldReturnAllTaskStatistics() {
        when(taskService.countTasks(testUser))
                .thenReturn(10L);

        when(taskService.countTasksByStatus(TaskStatus.TODO, testUser))
                .thenReturn(4L);

        when(taskService.countTasksByStatus(
                TaskStatus.IN_PROGRESS,
                testUser
        )).thenReturn(3L);

        when(taskService.countTasksByStatus(TaskStatus.DONE, testUser))
                .thenReturn(3L);

        when(taskService.countTasksByPriority(
                TaskPriority.LOW,
                testUser
        )).thenReturn(2L);

        when(taskService.countTasksByPriority(
                TaskPriority.MEDIUM,
                testUser
        )).thenReturn(5L);

        when(taskService.countTasksByPriority(
                TaskPriority.HIGH,
                testUser
        )).thenReturn(3L);

        when(taskService.countOverdueTasks(testUser))
                .thenReturn(1L);

        DashboardResponse result =
                dashboardService.getDashboard(testUser);

        assertEquals(10L, result.getTotalTasks());
        assertEquals(4L, result.getTodoTasks());
        assertEquals(3L, result.getInProgressTasks());
        assertEquals(3L, result.getDoneTasks());
        assertEquals(2L, result.getLowPriorityTasks());
        assertEquals(5L, result.getMediumPriorityTasks());
        assertEquals(3L, result.getHighPriorityTasks());
        assertEquals(1L, result.getOverdueTasks());

        verify(taskService).countTasks(testUser);

        verify(taskService).countTasksByStatus(
                TaskStatus.TODO,
                testUser
        );

        verify(taskService).countTasksByStatus(
                TaskStatus.IN_PROGRESS,
                testUser
        );

        verify(taskService).countTasksByStatus(
                TaskStatus.DONE,
                testUser
        );

        verify(taskService).countTasksByPriority(
                TaskPriority.LOW,
                testUser
        );

        verify(taskService).countTasksByPriority(
                TaskPriority.MEDIUM,
                testUser
        );

        verify(taskService).countTasksByPriority(
                TaskPriority.HIGH,
                testUser
        );

        verify(taskService).countOverdueTasks(testUser);
    }
}
