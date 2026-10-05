package com.eva.evagita.service.impl;

import com.eva.evagita.exception.TagNotFoundException;
import com.eva.evagita.model.Tag;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.TagRepository;
import com.eva.evagita.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public Tag createTag(Tag tag, User user) {
        if (tagRepository.existsByNameAndUser(tag.getName(), user)) {
            throw new IllegalArgumentException(
                    "Tag with name '" + tag.getName() + "' already exists"
            );
        }

        tag.setUser(user);

        return tagRepository.save(tag);
    }

    @Override
    public List<Tag> getAllTags(User user) {
        return tagRepository.findAllByUser(user);
    }

    @Override
    public Tag getTagById(Long id, User user) {
        return tagRepository.findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new TagNotFoundException(
                                "Tag not found with id: " + id
                        ));
    }

    @Override
    public Tag getTagByName(String name, User user) {
        return tagRepository.findByNameAndUser(name, user)
                .orElseThrow(() ->
                        new TagNotFoundException(
                                "Tag not found with name: " + name
                        ));
    }

    @Override
    public Tag updateTag(Long id, Tag tag, User user) {
        Tag existingTag = getTagById(id, user);

        if (!existingTag.getName().equals(tag.getName())
                && tagRepository.existsByNameAndUser(tag.getName(), user)) {
            throw new IllegalArgumentException(
                    "Tag with name '" + tag.getName() + "' already exists"
            );
        }

        existingTag.setName(tag.getName());

        return tagRepository.save(existingTag);
    }

    @Override
    public void deleteTag(Long id, User user) {
        Tag existingTag = getTagById(id, user);
        tagRepository.delete(existingTag);
    }
}
