package com.eva.evagita.service;

import com.eva.evagita.model.Tag;
import com.eva.evagita.model.User;

import java.util.List;

public interface TagService {

    Tag createTag(Tag tag, User user);

    List<Tag> getAllTags(User user);

    Tag getTagById(Long id, User user);

    Tag getTagByName(String name, User user);

    Tag updateTag(Long id, Tag tag, User user);

    void deleteTag(Long id, User user);
}
