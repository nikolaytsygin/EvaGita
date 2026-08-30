package com.eva.evagita.repository;

import com.eva.evagita.PostgresIntegrationTest;
import com.eva.evagita.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TagRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();
    }

    @Test
    void shouldSaveAndReadTag() {
        Tag tag = new Tag();
        tag.setName("backend");

        Tag savedTag = tagRepository.save(tag);

        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getName()).isEqualTo("backend");

        Tag foundTag = tagRepository.findById(savedTag.getId())
                .orElseThrow();

        assertThat(foundTag.getName()).isEqualTo("backend");
    }

    @Test
    void shouldFindTagByName() {
        Tag tag = new Tag();
        tag.setName("java");

        tagRepository.save(tag);

        assertThat(tagRepository.findByName("java"))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("java");
    }

    @Test
    void shouldReturnEmptyWhenTagNameDoesNotExist() {
        assertThat(tagRepository.findByName("non-existent"))
                .isEmpty();
    }

    @Test
    void shouldCheckTagExistenceByName() {
        Tag tag = new Tag();
        tag.setName("testing");

        tagRepository.save(tag);

        assertThat(tagRepository.existsByName("testing"))
                .isTrue();

        assertThat(tagRepository.existsByName("non-existent"))
                .isFalse();
    }
}
