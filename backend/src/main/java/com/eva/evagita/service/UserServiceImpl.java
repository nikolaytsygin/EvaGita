package com.eva.evagita.service;

import com.eva.evagita.exception.UserNotFoundException;
import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.security.PasswordHashService;
import com.eva.evagita.security.LoginRateLimiter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordHashService passwordHashService;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordHashService passwordHashService
    ) {
        this.userRepository = userRepository;
        this.passwordHashService = passwordHashService;
    }

    @Override
    public User createUser(User user) {
        validateUser(user);
        user.setEmail(LoginRateLimiter.normalizeEmail(user.getEmail()));
        user.setPassword(passwordHashService.encodeIfNeeded(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User getCurrentUser(String username) {
        return getUserByUsername(username);
    }

    @Override
    @Transactional
    public User updateCurrentUser(
            String currentUsername,
            String newUsername,
            String email
    ) {
        User existingUser = userRepository.findByUsernameForUpdate(currentUsername)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with username: " + currentUsername));

        validateProfile(newUsername, email);

        String normalizedEmail = LoginRateLimiter.normalizeEmail(email);
        boolean identityChanged = !existingUser.getUsername().equals(newUsername)
                || !existingUser.getEmail().equals(normalizedEmail);

        existingUser.setUsername(newUsername);
        existingUser.setEmail(normalizedEmail);
        if (identityChanged) {
            existingUser.revokeTokens();
        }

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void changePassword(
            String username,
            String currentPassword,
            String newPassword
    ) {
        User existingUser = userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with username: " + username));

        if (!passwordHashService.matches(currentPassword, existingUser.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password must not be empty");
        }

        existingUser.setPassword(passwordHashService.encodeIfNeeded(newPassword));
        existingUser.revokeTokens();
        userRepository.save(existingUser);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with username: " + username));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with email: " + email));
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);

        validateUser(user);

        String normalizedEmail = LoginRateLimiter.normalizeEmail(user.getEmail());
        String encodedPassword = passwordHashService.encodeIfNeeded(user.getPassword());
        boolean changed = !existingUser.getUsername().equals(user.getUsername())
                || !existingUser.getEmail().equals(normalizedEmail)
                || !existingUser.getPassword().equals(encodedPassword);

        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(normalizedEmail);
        existingUser.setPassword(encodedPassword);
        if (changed) {
            existingUser.revokeTokens();
        }

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        User existingUser = getUserById(id);
        userRepository.delete(existingUser);
    }

    private void validateUser(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email must not be empty");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
    }

    private void validateProfile(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be empty");
        }
    }
}
