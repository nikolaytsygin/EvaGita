package com.eva.evagita.service;

import com.eva.evagita.model.User;

import java.util.List;

public interface UserService {

    User createUser(User user);

    User getCurrentUser(String username);

    User updateCurrentUser(String currentUsername, String newUsername, String email);

    void changePassword(String username, String currentPassword, String newPassword);

    List<User> getAllUsers();

    User getUserById(Long id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User updateUser(Long id, User user);

    void deleteUser(Long id);
}
