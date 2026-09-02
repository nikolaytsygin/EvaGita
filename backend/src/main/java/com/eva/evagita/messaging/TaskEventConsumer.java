package com.eva.evagita.messaging;

import com.eva.evagita.config.RabbitMQConfig;
import com.eva.evagita.event.TaskEvent;
import com.eva.evagita.model.NotificationType;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TaskEventConsumer {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public TaskEventConsumer(
            NotificationService notificationService,
            UserRepository userRepository
    ) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consume(TaskEvent event) {
        User user = userRepository.findById(event.userId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + event.userId()
                        )
                );

        String message = buildMessage(event);

        notificationService.createNotification(
                user,
                event.type(),
                message
        );
    }

    private String buildMessage(TaskEvent event) {
        return switch (event.type()) {
            case TASK_CREATED ->
                    "Task created: " + event.taskTitle();

            case TASK_UPDATED ->
                    "Task updated: " + event.taskTitle();

            case TASK_COMPLETED ->
                    "Task completed: " + event.taskTitle();

            case TASK_OVERDUE ->
                    "Task overdue: " + event.taskTitle();
        };
    }
}
