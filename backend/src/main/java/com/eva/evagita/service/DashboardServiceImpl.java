package com.eva.evagita.service;

import com.eva.evagita.dto.DashboardResponse;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.User;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final TaskService taskService;

    public DashboardServiceImpl(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public DashboardResponse getDashboard(User user) {
        DashboardResponse response = new DashboardResponse();

        response.setTotalTasks(
                taskService.countTasks(user)
        );

        response.setTodoTasks(
                taskService.countTasksByStatus(
                        TaskStatus.TODO,
                        user
                )
        );

        response.setInProgressTasks(
                taskService.countTasksByStatus(
                        TaskStatus.IN_PROGRESS,
                        user
                )
        );

        response.setDoneTasks(
                taskService.countTasksByStatus(
                        TaskStatus.DONE,
                        user
                )
        );

        response.setLowPriorityTasks(
                taskService.countTasksByPriority(
                        TaskPriority.LOW,
                        user
                )
        );

        response.setMediumPriorityTasks(
                taskService.countTasksByPriority(
                        TaskPriority.MEDIUM,
                        user
                )
        );

        response.setHighPriorityTasks(
                taskService.countTasksByPriority(
                        TaskPriority.HIGH,
                        user
                )
        );

        response.setOverdueTasks(
                taskService.countOverdueTasks(user)
        );

        return response;
    }
}
