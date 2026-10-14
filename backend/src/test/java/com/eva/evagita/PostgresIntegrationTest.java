package com.eva.evagita;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.Container;

public abstract class PostgresIntegrationTest {

    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("evagita_test")
                    .withUsername("test")
                    .withPassword("test");

    static RabbitMQContainer rabbitmq =
            new RabbitMQContainer("rabbitmq:4-management");

    static {
        postgres.start();
        rabbitmq.start();
    }

    protected static void configureDeadLetterPolicy(
            String queueName,
            String exchangeName,
            String routingKey
    ) throws Exception {
        String definition = String.format(
                "{\"dead-letter-exchange\":\"%s\","
                        + "\"dead-letter-routing-key\":\"%s\"}",
                exchangeName,
                routingKey
        );

        Container.ExecResult result = rabbitmq.execInContainer(
                "rabbitmqctl",
                "set_policy",
                "--vhost", "/",
                "--priority", "10",
                "--apply-to", "queues",
                "evagita-test-dlx",
                "^" + queueName.replace(".", "\\.") + "$",
                definition
        );

        if (result.getExitCode() != 0) {
            throw new IllegalStateException(
                    "Failed to configure RabbitMQ DLX policy: "
                            + result.getStderr()
            );
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }
}
