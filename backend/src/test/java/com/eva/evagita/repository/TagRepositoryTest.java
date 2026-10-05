package com.eva.evagita.repository;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.model.Tag;
import com.eva.evagita.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TagRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();
    }

    private User createUser(String username, String email) {
        User user = new User(
                username,
                email,
                "password"
        );

        return userRepository.save(user);
    }

    @Test
    void shouldSaveAndReadTag() {
        User user = createUser(
                "tag-save-user",
                "tag-save@example.com"
        );

        Tag tag = new Tag();
        tag.setName("backend");
        tag.setUser(user);

        Tag savedTag = tagRepository.save(tag);

        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getName()).isEqualTo("backend");
        assertThat(savedTag.getUser()).isEqualTo(user);

        Tag foundTag = tagRepository.findByIdAndUser(
                        savedTag.getId(),
                        user
                )
                .orElseThrow();

        assertThat(foundTag.getName()).isEqualTo("backend");
        assertThat(foundTag.getUser()).isEqualTo(user);
    }

    @Test
    void shouldFindTagByNameAndUser() {
        User user = createUser(
                "tag-name-user",
                "tag-name@example.com"
        );

        Tag tag = new Tag();
        tag.setName("java");
        tag.setUser(user);

        tagRepository.save(tag);

        assertThat(tagRepository.findByNameAndUser("java", user))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("java");
    }

    @Test
    void shouldReturnEmptyWhenTagNameDoesNotExistForUser() {
        User user = createUser(
                "tag-empty-user",
                "tag-empty@example.com"
        );

        assertThat(tagRepository.findByNameAndUser(
                "non-existent",
                user
        )).isEmpty();
    }

    @Test
    void shouldCheckTagExistenceByNameAndUser() {
        User user = createUser(
                "tag-exists-user",
                "tag-exists@example.com"
        );

        Tag tag = new Tag();
        tag.setName("testing");
        tag.setUser(user);

        tagRepository.save(tag);

        assertThat(
                tagRepository.existsByNameAndUser(
                        "testing",
                        user
                )
        ).isTrue();

        assertThat(
                tagRepository.existsByNameAndUser(
                        "non-existent",
                        user
                )
        ).isFalse();
    }

    @Test
    void shouldNotFindTagFromAnotherUser() {
        User user1 = createUser(
                "tag-owner-one",
                "tag-owner-one@example.com"
        );

        User user2 = createUser(
                "tag-owner-two",
                "tag-owner-two@example.com"
        );

        Tag tag = new Tag();
        tag.setName("backend");
        tag.setUser(user1);

        tagRepository.save(tag);

        assertThat(
                tagRepository.findByNameAndUser(
                        "backend",
                        user1
                )
        ).isPresent();

        assertThat(
                tagRepository.findByNameAndUser(
                        "backend",
                        user2
                )
        ).isEmpty();

        assertThat(
                tagRepository.existsByNameAndUser(
                        "backend",
                        user1
                )
        ).isTrue();

        assertThat(
                tagRepository.existsByNameAndUser(
                        "backend",
                        user2
                )
        ).isFalse();
    }
}
