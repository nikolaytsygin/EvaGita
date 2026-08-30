package com.eva.evagita.service.impl;

import com.eva.evagita.exception.TagNotFoundException;
import com.eva.evagita.model.Tag;
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

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(tagRepository);
    }

    @Test
    void createTag_shouldSaveAndReturnTag() {
        Tag tag = new Tag();
        tag.setName("backend");

        when(tagRepository.existsByName("backend"))
                .thenReturn(false);
        when(tagRepository.save(tag))
                .thenReturn(tag);

        Tag result = tagService.createTag(tag);

        assertSame(tag, result);
        verify(tagRepository).existsByName("backend");
        verify(tagRepository).save(tag);
    }

    @Test
    void createTag_shouldThrowWhenTagNameAlreadyExists() {
        Tag tag = new Tag();
        tag.setName("backend");

        when(tagRepository.existsByName("backend"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.createTag(tag)
        );

        assertEquals(
                "Tag with name 'backend' already exists",
                exception.getMessage()
        );

        verify(tagRepository).existsByName("backend");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void getAllTags_shouldReturnAllTags() {
        Tag firstTag = new Tag();
        firstTag.setName("backend");

        Tag secondTag = new Tag();
        secondTag.setName("frontend");

        List<Tag> tags = List.of(firstTag, secondTag);

        when(tagRepository.findAll()).thenReturn(tags);

        List<Tag> result = tagService.getAllTags();

        assertEquals(2, result.size());
        assertEquals(tags, result);
        verify(tagRepository).findAll();
    }

    @Test
    void getTagById_shouldReturnTagWhenExists() {
        Long id = 1L;

        Tag tag = new Tag();
        tag.setId(id);
        tag.setName("backend");

        when(tagRepository.findById(id))
                .thenReturn(Optional.of(tag));

        Tag result = tagService.getTagById(id);

        assertSame(tag, result);
        verify(tagRepository).findById(id);
    }

    @Test
    void getTagById_shouldThrowWhenTagDoesNotExist() {
        Long id = 999L;

        when(tagRepository.findById(id))
                .thenReturn(Optional.empty());

        TagNotFoundException exception = assertThrows(
                TagNotFoundException.class,
                () -> tagService.getTagById(id)
        );

        assertEquals(
                "Tag not found with id: " + id,
                exception.getMessage()
        );

        verify(tagRepository).findById(id);
    }

    @Test
    void getTagByName_shouldReturnTagWhenExists() {
        Tag tag = new Tag();
        tag.setName("backend");

        when(tagRepository.findByName("backend"))
                .thenReturn(Optional.of(tag));

        Tag result = tagService.getTagByName("backend");

        assertSame(tag, result);
        verify(tagRepository).findByName("backend");
    }

    @Test
    void getTagByName_shouldThrowWhenTagDoesNotExist() {
        when(tagRepository.findByName("unknown"))
                .thenReturn(Optional.empty());

        TagNotFoundException exception = assertThrows(
                TagNotFoundException.class,
                () -> tagService.getTagByName("unknown")
        );

        assertEquals(
                "Tag not found with name: unknown",
                exception.getMessage()
        );

        verify(tagRepository).findByName("unknown");
    }

    @Test
    void updateTag_shouldUpdateNameAndSaveTag() {
        Long id = 1L;

        Tag existingTag = new Tag();
        existingTag.setId(id);
        existingTag.setName("old-name");

        Tag updatedTag = new Tag();
        updatedTag.setName("new-name");

        when(tagRepository.findById(id))
                .thenReturn(Optional.of(existingTag));
        when(tagRepository.existsByName("new-name"))
                .thenReturn(false);
        when(tagRepository.save(existingTag))
                .thenReturn(existingTag);

        Tag result = tagService.updateTag(id, updatedTag);

        assertEquals("new-name", existingTag.getName());
        assertSame(existingTag, result);

        verify(tagRepository).findById(id);
        verify(tagRepository).existsByName("new-name");
        verify(tagRepository).save(existingTag);
    }

    @Test
    void updateTag_shouldNotCheckDuplicateWhenNameIsUnchanged() {
        Long id = 1L;

        Tag existingTag = new Tag();
        existingTag.setId(id);
        existingTag.setName("backend");

        Tag updatedTag = new Tag();
        updatedTag.setName("backend");

        when(tagRepository.findById(id))
                .thenReturn(Optional.of(existingTag));
        when(tagRepository.save(existingTag))
                .thenReturn(existingTag);

        Tag result = tagService.updateTag(id, updatedTag);

        assertSame(existingTag, result);
        assertEquals("backend", existingTag.getName());

        verify(tagRepository).findById(id);
        verify(tagRepository, never()).existsByName(anyString());
        verify(tagRepository).save(existingTag);
    }

    @Test
    void updateTag_shouldThrowWhenNewNameAlreadyExists() {
        Long id = 1L;

        Tag existingTag = new Tag();
        existingTag.setId(id);
        existingTag.setName("backend");

        Tag updatedTag = new Tag();
        updatedTag.setName("frontend");

        when(tagRepository.findById(id))
                .thenReturn(Optional.of(existingTag));
        when(tagRepository.existsByName("frontend"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.updateTag(id, updatedTag)
        );

        assertEquals(
                "Tag with name 'frontend' already exists",
                exception.getMessage()
        );

        assertEquals("backend", existingTag.getName());

        verify(tagRepository).findById(id);
        verify(tagRepository).existsByName("frontend");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void deleteTag_shouldFindAndDeleteTag() {
        Long id = 1L;

        Tag tag = new Tag();
        tag.setId(id);
        tag.setName("backend");

        when(tagRepository.findById(id))
                .thenReturn(Optional.of(tag));

        tagService.deleteTag(id);

        verify(tagRepository).findById(id);
        verify(tagRepository).delete(tag);
    }

    @Test
    void deleteTag_shouldThrowWhenTagDoesNotExist() {
        Long id = 999L;

        when(tagRepository.findById(id))
                .thenReturn(Optional.empty());

        TagNotFoundException exception = assertThrows(
                TagNotFoundException.class,
                () -> tagService.deleteTag(id)
        );

        assertEquals(
                "Tag not found with id: " + id,
                exception.getMessage()
        );

        verify(tagRepository).findById(id);
        verify(tagRepository, never()).delete(any(Tag.class));
    }
}
