package com.eva.evagita.controller;

import com.eva.evagita.model.Tag;
import com.eva.evagita.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(tagController)
                .build();
    }

    @Test
    void getAllTags_shouldReturn200AndTags() throws Exception {
        Tag firstTag = new Tag();
        firstTag.setId(1L);
        firstTag.setName("backend");

        Tag secondTag = new Tag();
        secondTag.setId(2L);
        secondTag.setName("frontend");

        when(tagService.getAllTags())
                .thenReturn(List.of(firstTag, secondTag));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("backend"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("frontend"));

        verify(tagService).getAllTags();
    }

    @Test
    void getTagById_shouldReturn200AndTag() throws Exception {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("backend");

        when(tagService.getTagById(1L))
                .thenReturn(tag);

        mockMvc.perform(get("/api/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("backend"));

        verify(tagService).getTagById(1L);
    }

    @Test
    void createTag_shouldReturn201AndCreatedTag() throws Exception {
        Tag createdTag = new Tag();
        createdTag.setId(1L);
        createdTag.setName("backend");

        when(tagService.createTag(any(Tag.class)))
                .thenReturn(createdTag);

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "backend"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("backend"));

        verify(tagService).createTag(any(Tag.class));
    }

    @Test
    void createTag_shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(tagService, never()).createTag(any(Tag.class));
    }

    @Test
    void updateTag_shouldReturn200AndUpdatedTag() throws Exception {
        Tag updatedTag = new Tag();
        updatedTag.setId(1L);
        updatedTag.setName("updated");

        when(tagService.updateTag(eq(1L), any(Tag.class)))
                .thenReturn(updatedTag);

        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("updated"));

        verify(tagService).updateTag(eq(1L), any(Tag.class));
    }

    @Test
    void updateTag_shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(tagService, never())
                .updateTag(anyLong(), any(Tag.class));
    }

    @Test
    void deleteTag_shouldReturn204() throws Exception {
        doNothing().when(tagService).deleteTag(1L);

        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isNoContent());

        verify(tagService).deleteTag(1L);
    }
}
