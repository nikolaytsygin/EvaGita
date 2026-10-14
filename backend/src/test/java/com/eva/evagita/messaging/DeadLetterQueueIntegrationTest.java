package com.eva.evagita.messaging;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.config.RabbitMQConfig;
import com.eva.evagita.event.TaskEvent;
import com.eva.evagita.model.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DeadLetterQueueIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldSendUnprocessableEventToDeadLetterQueue() throws Exception {
        configureDeadLetterPolicy(
                RabbitMQConfig.NOTIFICATION_QUEUE,
                RabbitMQConfig.DEAD_LETTER_EXCHANGE,
                RabbitMQConfig.DEAD_LETTER_ROUTING_KEY
        );

        TaskEvent event = new TaskEvent(
                NotificationType.TASK_CREATED,
                Long.MAX_VALUE,
                987654L,
                "DLQ integration test"
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TASK_EXCHANGE,
                RabbitMQConfig.TASK_ROUTING_KEY,
                event
        );

        Message deadLetterMessage = null;

        for (int attempt = 0; attempt < 80; attempt++) {
            deadLetterMessage = rabbitTemplate.receive(
                    RabbitMQConfig.DEAD_LETTER_QUEUE
            );

            if (deadLetterMessage != null) {
                break;
            }

            Thread.sleep(250);
        }

        assertThat(deadLetterMessage)
                .as("Unprocessable event should arrive in the dead-letter queue")
                .isNotNull();

        assertThat(deadLetterMessage.getMessageProperties().getHeaders())
                .containsKey("x-death");
    }
}
