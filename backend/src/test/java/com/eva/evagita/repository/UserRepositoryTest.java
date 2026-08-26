package com.eva.evagita.repository;

import com.eva.evagita.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByUsername() {
        User user = new User(
                "testuser",
                "test@example.com",
                "password"
        );

        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldFindUserByEmail() {
        User user = new User(
                "emailuser",
                "email@example.com",
                "password"
        );

        userRepository.save(user);

        Optional<User> found =
                userRepository.findByEmail("email@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("emailuser");
    }

    @Test
    void shouldReturnEmptyWhenUsernameDoesNotExist() {
        Optional<User> found =
                userRepository.findByUsername("unknown");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<User> found =
                userRepository.findByEmail("unknown@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldSaveUserWithGeneratedId() {
        User user = new User(
                "generatedid",
                "generated@example.com",
                "password"
        );

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
    }
}
