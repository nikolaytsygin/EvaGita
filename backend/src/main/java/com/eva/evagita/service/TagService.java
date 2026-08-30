package com.eva.evagita.service;

import com.eva.evagita.model.Tag;

import java.util.List;

public interface TagService {

    Tag createTag(Tag tag);

    List<Tag> getAllTags();

    Tag getTagById(Long id);

    Tag getTagByName(String name);

    Tag updateTag(Long id, Tag tag);

    void deleteTag(Long id);
}
