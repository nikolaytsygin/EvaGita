package com.eva.evagita.service;

import com.eva.evagita.model.User;

import java.util.List;

public interface UserService {

    User createUser(User user);

    List<User> getAllUsers();

    User getUserById(Long id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User updateUser(Long id, User user);

    void deleteUser(Long id);
}
