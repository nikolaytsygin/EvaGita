package com.eva.evagita.messaging;

import com.eva.evagita.config.RabbitMQConfig;
import com.eva.evagita.event.TaskEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public TaskEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(TaskEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TASK_EXCHANGE,
                RabbitMQConfig.TASK_ROUTING_KEY,
                event
        );
    }
}
