package com.eva.evagita.controller;

import com.eva.evagita.dto.ChangePasswordRequest;
import com.eva.evagita.dto.UserRequest;
import com.eva.evagita.dto.UserProfileRequest;
import com.eva.evagita.dto.UserResponse;
import com.eva.evagita.model.User;
import com.eva.evagita.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        User user = toEntity(request);
        return toResponse(userService.createUser(user));
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return toResponse(userService.getCurrentUser(currentUsername()));
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(
            @Valid @RequestBody UserProfileRequest request
    ) {
        return toResponse(userService.updateCurrentUser(
                currentUsername(),
                request.getUsername(),
                request.getEmail()
        ));
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(
                currentUsername(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void rejectUserLookup(@PathVariable String id) {
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void rejectUserUpdate(@PathVariable String id) {
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void rejectUserDeletion(@PathVariable String id) {
    }

    private String currentUsername() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private User toEntity(UserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        return user;
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
