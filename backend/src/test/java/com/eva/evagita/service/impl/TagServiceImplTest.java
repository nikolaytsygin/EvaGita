package com.eva.evagita.service.impl;

import com.eva.evagita.exception.TagNotFoundException;
import com.eva.evagita.model.Tag;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    private TagServiceImpl tagService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(tagRepository);

        user1 = new User();
        user1.setUsername("user1");

        user2 = new User();
        user2.setUsername("user2");
    }

    @Test
    void createTag_shouldSaveAndReturnTagForUser() {
        Tag tag = new Tag();
        tag.setName("backend");

        when(tagRepository.existsByNameAndUser("backend", user1))
                .thenReturn(false);
        when(tagRepository.save(tag))
                .thenReturn(tag);

        Tag result = tagService.createTag(tag, user1);

        assertSame(tag, result);
        assertSame(user1, tag.getUser());

        verify(tagRepository).existsByNameAndUser("backend", user1);
        verify(tagRepository).save(tag);
    }

    @Test
    void createTag_shouldThrowWhenTagNameAlreadyExistsForSameUser() {
        Tag tag = new Tag();
        tag.setName("backend");

        when(tagRepository.existsByNameAndUser("backend", user1))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.createTag(tag, user1)
        );

        assertEquals(
                "Tag with name 'backend' already exists",
                exception.getMessage()
        );

        verify(tagRepository).existsByNameAndUser("backend", user1);
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTag_shouldAllowSameNameForDifferentUsers() {
        Tag tag1 = new Tag();
        tag1.setName("backend");

        Tag tag2 = new Tag();
        tag2.setName("backend");

        when(tagRepository.existsByNameAndUser("backend", user1))
                .thenReturn(false);

        when(tagRepository.existsByNameAndUser("backend", user2))
                .thenReturn(false);

        when(tagRepository.save(tag1))
                .thenReturn(tag1);

        when(tagRepository.save(tag2))
                .thenReturn(tag2);

        Tag result1 = tagService.createTag(tag1, user1);
        Tag result2 = tagService.createTag(tag2, user2);

        assertSame(tag1, result1);
        assertSame(tag2, result2);

        assertSame(user1, tag1.getUser());
        assertSame(user2, tag2.getUser());

        verify(tagRepository)
                .existsByNameAndUser("backend", user1);

        verify(tagRepository)
                .existsByNameAndUser("backend", user2);

        verify(tagRepository).save(tag1);
        verify(tagRepository).save(tag2);
    }

    @Test
    void getAllTags_shouldReturnOnlyTagsForUser() {
        Tag firstTag = new Tag();
        firstTag.setName("backend");
        firstTag.setUser(user1);

        Tag secondTag = new Tag();
        secondTag.setName("frontend");
        secondTag.setUser(user1);

        List<Tag> tags = List.of(firstTag, secondTag);

        when(tagRepository.findAllByUser(user1))
                .thenReturn(tags);

        List<Tag> result = tagService.getAllTags(user1);

        assertEquals(2, result.size());
        assertEquals(tags, result);

        verify(tagRepository).findAllByUser(user1);
    }

    @Test
    void getTagById_shouldReturnTagWhenBelongsToUser() {
        Long id = 1L;

        Tag tag = new Tag();
        tag.setId(id);
        tag.setName("backend");
        tag.setUser(user1);

        when(tagRepository.findByIdAndUser(id, user1))
                .thenReturn(Optional.of(tag));

        Tag result = tagService.getTagById(id, user1);

        assertSame(tag, result);

        verify(tagRepository).findByIdAndUser(id, user1);
    }

    @Test
    void getTagById_shouldThrowWhenTagDoesNotBelongToUser() {
        Long id = 999L;

        when(tagRepository.findByIdAndUser(id, user1))
                .thenReturn(Optional.empty());

        TagNotFoundException exception = assertThrows(
                TagNotFoundException.class,
                () -> tagService.getTagById(id, user1)
        );

        assertEquals(
                "Tag not found with id: " + id,
                exception.getMessage()
        );

        verify(tagRepository).findByIdAndUser(id, user1);
    }

    @Test
    void getTagByName_shouldReturnTagWhenBelongsToUser() {
        Tag tag = new Tag();
        tag.setName("backend");
        tag.setUser(user1);

        when(tagRepository.findByNameAndUser("backend", user1))
                .thenReturn(Optional.of(tag));

        Tag result = tagService.getTagByName("backend", user1);

        assertSame(tag, result);

        verify(tagRepository)
                .findByNameAndUser("backend", user1);
    }

    @Test
    void getTagByName_shouldThrowWhenTagDoesNotBelongToUser() {
        when(tagRepository.findByNameAndUser("unknown", user1))
                .thenReturn(Optional.empty());

        TagNotFoundException exception = assertThrows(
                TagNotFoundException.class,
                () -> tagService.getTagByName("unknown", user1)
        );

        assertEquals(
                "Tag not found with name: unknown",
                exception.getMessage()
        );

        verify(tagRepository)
                .findByNameAndUser("unknown", user1);
    }

    @Test
    void updateTag_shouldUpdateNameAndSaveTagForUser() {
        Long id = 1L;

        Tag existingTag = new Tag();
        existingTag.setId(id);
        existingTag.setName("old-name");
        existingTag.setUser(user1);

        Tag updatedTag = new Tag();
        updatedTag.setName("new-name");

        when(tagRepository.findByIdAndUser(id, user1))
                .thenReturn(Optional.of(existingTag));

        when(tagRepository.existsByNameAndUser("new-name", user1))
                .thenReturn(false);

        when(tagRepository.save(existingTag))
                .thenReturn(existingTag);

        Tag result = tagService.updateTag(
                id,
                updatedTag,
                user1
        );

        assertEquals("new-name", existingTag.getName());
        assertSame(existingTag, result);

        verify(tagRepository)
                .findByIdAndUser(id, user1);

        verify(tagRepository)
                .existsByNameAndUser("new-name", user1);

        verify(tagRepository)
                .save(existingTag);
    }

    @Test
    void updateTag_shouldNotCheckDuplicateWhenNameIsUnchanged() {
        Long id = 1L;

        Tag existingTag = new Tag();
        existingTag.setId(id);
        existingTag.setName("backend");
        existingTag.setUser(user1);

        Tag updatedTag = new Tag();
        updatedTag.setName("backend");

        when(tagRepository.findByIdAndUser(id, user1))
                .thenReturn(Optional.of(existingTag));

        when(tagRepository.save(existingTag))
                .thenReturn(existingTag);

        Tag result = tagService.updateTag(
                id,
                updatedTag,
                user1
        );

        assertSame(existingTag, result);
        assertEquals("backend", existingTag.getName());

        verify(tagRepository)
                .findByIdAndUser(id, user1);

        verify(tagRepository, never())
                .existsByNameAndUser(
                        anyString(),
                        any(User.class)
                );

        verify(tagRepository)
                .save(existingTag);
    }

    @Test
    void updateTag_shouldThrowWhenNewNameAlreadyExistsForSameUser() {
        Long id = 1L;

        Tag existingTag = new Tag();
        existingTag.setId(id);
        existingTag.setName("backend");
        existingTag.setUser(user1);

        Tag updatedTag = new Tag();
        updatedTag.setName("frontend");

        when(tagRepository.findByIdAndUser(id, user1))
                .thenReturn(Optional.of(existingTag));

        when(tagRepository.existsByNameAndUser("frontend", user1))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.updateTag(
                        id,
                        updatedTag,
                        user1
                )
        );

        assertEquals(
                "Tag with name 'frontend' already exists",
                exception.getMessage()
        );

        assertEquals("backend", existingTag.getName());

        verify(tagRepository)
                .findByIdAndUser(id, user1);

        verify(tagRepository)
                .existsByNameAndUser("frontend", user1);

        verify(tagRepository, never())
                .save(any(Tag.class));
    }

    @Test
    void deleteTag_shouldFindAndDeleteTagForUser() {
        Long id = 1L;

        Tag tag = new Tag();
        tag.setId(id);
        tag.setName("backend");
        tag.setUser(user1);

        when(tagRepository.findByIdAndUser(id, user1))
                .thenReturn(Optional.of(tag));

        tagService.deleteTag(id, user1);

        verify(tagRepository)
                .findByIdAndUser(id, user1);

        verify(tagRepository)
                .delete(tag);
    }

    @Test
    void deleteTag_shouldThrowWhenTagDoesNotBelongToUser() {
        Long id = 999L;

        when(tagRepository.findByIdAndUser(id, user1))
                .thenReturn(Optional.empty());

        TagNotFoundException exception = assertThrows(
                TagNotFoundException.class,
                () -> tagService.deleteTag(id, user1)
        );

        assertEquals(
                "Tag not found with id: " + id,
                exception.getMessage()
        );

        verify(tagRepository)
                .findByIdAndUser(id, user1);

        verify(tagRepository, never())
                .delete(any(Tag.class));
    }
}
