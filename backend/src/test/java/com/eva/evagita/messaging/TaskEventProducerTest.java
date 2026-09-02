package com.eva.evagita.messaging;

import com.eva.evagita.config.RabbitMQConfig;
import com.eva.evagita.event.TaskEvent;
import com.eva.evagita.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskEventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private TaskEventProducer taskEventProducer;

    @BeforeEach
    void setUp() {
        taskEventProducer = new TaskEventProducer(rabbitTemplate);
    }

    @Test
    void send_shouldPublishTaskEventToRabbitMq() {
        TaskEvent event = new TaskEvent(
                NotificationType.TASK_CREATED,
                1L,
                10L,
                "Test task"
        );

        taskEventProducer.send(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.TASK_EXCHANGE,
                RabbitMQConfig.TASK_ROUTING_KEY,
                event
        );
    }
}
