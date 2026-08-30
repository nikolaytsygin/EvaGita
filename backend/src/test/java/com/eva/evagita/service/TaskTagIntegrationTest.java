package com.eva.evagita.service;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.model.Tag;
import com.eva.evagita.model.Task;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.TagRepository;
import com.eva.evagita.repository.TaskRepository;
import com.eva.evagita.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskTagIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(
                new User(
                        "task-tag-test-user",
                        "task-tag-test@example.com",
                        "password"
                )
        );
    }

    @Test
    void shouldSaveTaskWithTagAndReadRelationship() {
        Tag tag = new Tag();
        tag.setName("backend");
        tag = tagRepository.save(tag);

        Task task = new Task();
        task.setTitle("Task with tag");
        task.setUser(testUser);
        task.getTags().add(tag);

        Task savedTask = taskRepository.saveAndFlush(task);

        Task foundTask = taskRepository.findByIdAndUser(
                savedTask.getId(),
                testUser
        ).orElseThrow();

        assertThat(foundTask.getTags())
                .hasSize(1)
                .extracting(Tag::getName)
                .containsExactly("backend");
    }

    @Test
    void shouldSaveMultipleTagsForOneTask() {
        Tag backendTag = new Tag();
        backendTag.setName("backend");

        Tag javaTag = new Tag();
        javaTag.setName("java");

        tagRepository.saveAll(
                Set.of(backendTag, javaTag)
        );

        Task task = new Task();
        task.setTitle("Java backend task");
        task.setUser(testUser);
        task.getTags().add(backendTag);
        task.getTags().add(javaTag);

        Task savedTask = taskRepository.saveAndFlush(task);

        Task foundTask = taskRepository.findByIdAndUser(
                savedTask.getId(),
                testUser
        ).orElseThrow();

        assertThat(foundTask.getTags())
                .hasSize(2)
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder(
                        "backend",
                        "java"
                );
    }

    @Test
    void shouldRemoveTagRelationshipWithoutDeletingTag() {
        Tag tag = new Tag();
        tag.setName("temporary");
        tag = tagRepository.save(tag);

        Task task = new Task();
        task.setTitle("Task with removable tag");
        task.setUser(testUser);
        task.getTags().add(tag);

        Task savedTask = taskRepository.saveAndFlush(task);

        savedTask.getTags().remove(tag);
        taskRepository.saveAndFlush(savedTask);

        Task foundTask = taskRepository.findByIdAndUser(
                savedTask.getId(),
                testUser
        ).orElseThrow();

        assertThat(foundTask.getTags()).isEmpty();

        assertThat(tagRepository.findById(tag.getId()))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("temporary");
    }

    @Test
    void shouldKeepTagSharedBetweenDifferentTasks() {
        Tag tag = new Tag();
        tag.setName("shared");
        tag = tagRepository.save(tag);

        Task firstTask = new Task();
        firstTask.setTitle("First task");
        firstTask.setUser(testUser);
        firstTask.getTags().add(tag);

        Task secondTask = new Task();
        secondTask.setTitle("Second task");
        secondTask.setUser(testUser);
        secondTask.getTags().add(tag);

        firstTask = taskRepository.saveAndFlush(firstTask);
        secondTask = taskRepository.saveAndFlush(secondTask);

        Task foundFirstTask = taskRepository.findByIdAndUser(
                firstTask.getId(),
                testUser
        ).orElseThrow();

        Task foundSecondTask = taskRepository.findByIdAndUser(
                secondTask.getId(),
                testUser
        ).orElseThrow();

        assertThat(foundFirstTask.getTags())
                .extracting(Tag::getName)
                .containsExactly("shared");

        assertThat(foundSecondTask.getTags())
                .extracting(Tag::getName)
                .containsExactly("shared");

        assertThat(tagRepository.findById(tag.getId()))
                .isPresent();
    }
}
