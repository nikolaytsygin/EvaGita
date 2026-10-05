package com.eva.evagita.controller;

import com.eva.evagita.exception.UserNotFoundException;
import com.eva.evagita.model.Tag;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;
    private final UserRepository userRepository;

    public TagController(
            TagService tagService,
            UserRepository userRepository
    ) {
        this.tagService = tagService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        User currentUser = getCurrentUser();

        return ResponseEntity.ok(
                tagService.getAllTags(currentUser)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        User currentUser = getCurrentUser();

        return ResponseEntity.ok(
                tagService.getTagById(id, currentUser)
        );
    }

    @PostMapping
    public ResponseEntity<Tag> createTag(
            @Valid @RequestBody Tag tag
    ) {
        User currentUser = getCurrentUser();

        Tag createdTag = tagService.createTag(tag, currentUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTag);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody Tag tag
    ) {
        User currentUser = getCurrentUser();

        return ResponseEntity.ok(
                tagService.updateTag(id, tag, currentUser)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long id
    ) {
        User currentUser = getCurrentUser();

        tagService.deleteTag(id, currentUser);

        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException(authentication.getName()));
    }
}
