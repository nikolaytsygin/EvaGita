package com.eva.evagita.service;

import com.eva.evagita.event.TaskEvent;
import com.eva.evagita.messaging.TaskEventProducer;
import com.eva.evagita.model.NotificationType;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.repository.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class OverdueTaskNotificationService {

    private final TaskRepository taskRepository;
    private final TaskEventProducer taskEventProducer;

    public OverdueTaskNotificationService(
            TaskRepository taskRepository,
            TaskEventProducer taskEventProducer
    ) {
        this.taskRepository = taskRepository;
        this.taskEventProducer = taskEventProducer;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void notifyOverdueTasks() {
        List<Task> overdueTasks =
                taskRepository.findAllByDueDateBeforeAndStatusNot(
                        LocalDate.now(),
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
