package com.eva.evagita.service;

import com.eva.evagita.event.TaskEvent;
import com.eva.evagita.messaging.TaskEventProducer;
import com.eva.evagita.model.NotificationType;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.repository.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OverdueTaskNotificationService {

    private final TaskRepository taskRepository;
    private final TaskEventProducer taskEventProducer;
    private final BusinessClock businessClock;

    public OverdueTaskNotificationService(
            TaskRepository taskRepository,
            TaskEventProducer taskEventProducer,
            BusinessClock businessClock
    ) {
        this.taskRepository = taskRepository;
        this.taskEventProducer = taskEventProducer;
        this.businessClock = businessClock;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void notifyOverdueTasks() {
        List<Task> overdueTasks =
                taskRepository.findAllByDueDateBeforeAndStatusNot(
                        businessClock.currentDate(),
                        TaskStatus.DONE
                );

        overdueTasks.forEach(task ->
                taskEventProducer.send(
                        new TaskEvent(
                                NotificationType.TASK_OVERDUE,
                                task.getUser().getId(),
                                task.getId(),
                                task.getTitle()
                        )
                )
        );
    }
}
