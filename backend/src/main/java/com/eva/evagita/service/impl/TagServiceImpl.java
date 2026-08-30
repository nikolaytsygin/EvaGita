package com.eva.evagita.service.impl;

import com.eva.evagita.exception.TagNotFoundException;
import com.eva.evagita.model.Tag;
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
    public Tag createTag(Tag tag) {
        if (tagRepository.existsByName(tag.getName())) {
            throw new IllegalArgumentException(
                    "Tag with name '" + tag.getName() + "' already exists"
            );
        }

        return tagRepository.save(tag);
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public Tag getTagById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() ->
                        new TagNotFoundException(
                                "Tag not found with id: " + id
                        ));
    }

    @Override
    public Tag getTagByName(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() ->
                        new TagNotFoundException(
                                "Tag not found with name: " + name
                        ));
    }

    @Override
    public Tag updateTag(Long id, Tag tag) {
        Tag existingTag = getTagById(id);

        if (!existingTag.getName().equals(tag.getName())
                && tagRepository.existsByName(tag.getName())) {
            throw new IllegalArgumentException(
                    "Tag with name '" + tag.getName() + "' already exists"
            );
        }

        existingTag.setName(tag.getName());

        return tagRepository.save(existingTag);
    }

    @Override
    public void deleteTag(Long id) {
        Tag existingTag = getTagById(id);
        tagRepository.delete(existingTag);
    }
}
