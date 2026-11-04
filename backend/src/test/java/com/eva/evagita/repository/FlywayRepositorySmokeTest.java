package com.eva.evagita.repository;

import com.eva.evagita.config.FlywayPostgresTestConfiguration;
import com.eva.evagita.model.Notification;
import com.eva.evagita.model.NotificationType;
import com.eva.evagita.model.Project;
import com.eva.evagita.model.Tag;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.TaskPriority;
import com.eva.evagita.model.TaskStatus;
import com.eva.evagita.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("migration-test")
@Import(FlywayRepositorySmokeTest.RabbitDisabledConfiguration.class)
@Transactional
class FlywayRepositorySmokeTest extends FlywayPostgresTestConfiguration {

    @TestConfiguration(proxyBeanMethods = false)
    static class RabbitDisabledConfiguration {

        @Bean(name = "rabbitListenerContainerFactory")
        SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
                ConnectionFactory connectionFactory,
                JacksonJsonMessageConverter messageConverter
        ) {
            SimpleRabbitListenerContainerFactory factory =
                    new SimpleRabbitListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            factory.setMessageConverter(messageConverter);
            factory.setAutoStartup(false);
            return factory;
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void persistsAndReadsUserOnFlywaySchema() {
        User user = new User(
                "flyway-user",
                "flyway-user@example.com",
                "hashed-password"
        );

        User saved = userRepository.saveAndFlush(user);

        User found = userRepository.findByUsername(saved.getUsername())
                .orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getEmail()).isEqualTo("flyway-user@example.com");
        assertThat(found.getTokenVersion()).isZero();
    }

    @Test
    void enforcesUserUniqueConstraintsOnFlywaySchema() {
        userRepository.saveAndFlush(new User(
                "unique-flyway-user",
                "unique-flyway-user@example.com",
                "hashed-password"
        ));

        assertThatThrownBy(() -> userRepository.saveAndFlush(new User(
                "unique-flyway-user",
                "another@example.com",
                "hashed-password"
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void persistsAndReadsProjectWithOwnerOnFlywaySchema() {
        User user = saveUser("project-owner", "project-owner@example.com");
        Project project = new Project();
        project.setName("Flyway project");
        project.setDescription("Project stored on the migrated schema");
        project.setUser(user);

        Project saved = projectRepository.saveAndFlush(project);
        Project found = projectRepository.findByIdAndUser(saved.getId(), user)
                .orElseThrow();

        assertThat(found.getName()).isEqualTo("Flyway project");
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void persistsAndReadsTagAndEnforcesPerUserUniquenessOnFlywaySchema() {
        User user = saveUser("tag-owner", "tag-owner@example.com");
        Tag tag = new Tag();
        tag.setName("flyway-tag");
        tag.setUser(user);

        Tag saved = tagRepository.saveAndFlush(tag);

        assertThat(tagRepository.findByNameAndUser("flyway-tag", user))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getId()).isEqualTo(saved.getId());
                    assertThat(found.getUser().getId()).isEqualTo(user.getId());
                });

        Tag duplicate = new Tag();
        duplicate.setName("flyway-tag");
        duplicate.setUser(user);

        assertThatThrownBy(() -> tagRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void persistsTaskWithOwnerProjectTagAndEnumsOnFlywaySchema() {
        User user = saveUser("task-owner", "task-owner@example.com");

        Project project = new Project();
        project.setName("Task project");
        project.setUser(user);
        projectRepository.saveAndFlush(project);

        Tag tag = new Tag();
        tag.setName("task-tag");
        tag.setUser(user);
        tagRepository.saveAndFlush(tag);

        Task task = new Task();
        task.setTitle("Migrated task");
        task.setDescription("Task with all mapped relations");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.of(2026, 11, 4));
        task.setUser(user);
        task.setProject(project);
        task.getTags().add(tag);

        Task saved = taskRepository.saveAndFlush(task);

        Task found = taskRepository.findAllByUser(user).stream()
                .filter(candidate -> candidate.getId().equals(saved.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(found.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(found.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
        assertThat(found.getProject().getId()).isEqualTo(project.getId());
        assertThat(found.getTags())
                .extracting(Tag::getId)
                .containsExactly(tag.getId());
    }

    @Test
    void persistsAndReadsNotificationWithOwnerOnFlywaySchema() {
        User user = saveUser("notification-owner", "notification-owner@example.com");
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.TASK_CREATED);
        notification.setMessage("Created on the migrated schema");
        notification.setRead(false);

        Notification saved = notificationRepository.saveAndFlush(notification);
        Notification found = notificationRepository
                .findByIdAndUser(saved.getId(), user)
                .orElseThrow();

        assertThat(found.getType()).isEqualTo(NotificationType.TASK_CREATED);
        assertThat(found.getMessage()).isEqualTo("Created on the migrated schema");
        assertThat(found.isRead()).isFalse();
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
    }

    private User saveUser(String username, String email) {
        return userRepository.saveAndFlush(new User(
                username,
                email,
                "hashed-password"
        ));
    }
}
