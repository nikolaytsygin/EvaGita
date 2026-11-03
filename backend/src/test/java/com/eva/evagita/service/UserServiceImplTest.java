package com.eva.evagita.service;

import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.security.PasswordHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(
                userRepository,
                new PasswordHashService(new BCryptPasswordEncoder())
        );
    }

    @Test
    void shouldHashPasswordWhenCreatingUser() {
        User user = new User("new-user", "new@example.com", "password");
        when(userRepository.save(user)).thenReturn(user);

        userService.createUser(user);

        assertThat(user.getPassword()).isNotEqualTo("password");
        assertThat(new BCryptPasswordEncoder().matches(
                "password",
                user.getPassword()
        )).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void shouldHashPasswordWhenUpdatingUser() {
        User existing = new User("existing", "existing@example.com", "old");
        User update = new User("existing", "existing@example.com", "new-password");

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        userService.updateUser(1L, update);

        assertThat(new BCryptPasswordEncoder().matches(
                "new-password",
                existing.getPassword()
        )).isTrue();
        verify(userRepository).save(existing);
    }

    @Test
    void shouldNotHashExistingBcryptValueAgain() {
        PasswordHashService passwordHashService = new PasswordHashService(
                new BCryptPasswordEncoder()
        );
        String existingHash = passwordHashService.encodeIfNeeded("password");
        User user = new User("existing", "existing@example.com", existingHash);
        when(userRepository.save(user)).thenReturn(user);

        userService.createUser(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo(existingHash);
    }

    @Test
    void shouldUpdateProfileForPrincipalUsername() {
        User existing = new User("user-a", "a@example.com", "hashed-password");
        when(userRepository.findByUsernameForUpdate("user-a"))
                .thenReturn(java.util.Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        userService.updateCurrentUser("user-a", "updated-user", "updated@example.com");

        assertThat(existing.getUsername()).isEqualTo("updated-user");
        assertThat(existing.getEmail()).isEqualTo("updated@example.com");
        assertThat(existing.getPassword()).isEqualTo("hashed-password");
        assertThat(existing.getTokenVersion()).isEqualTo(1L);
        verify(userRepository).save(existing);
    }

    @Test
    void shouldKeepTokenVersionWhenProfileDoesNotChange() {
        User existing = new User("user-a", "a@example.com", "hashed-password");
        when(userRepository.findByUsernameForUpdate("user-a"))
                .thenReturn(java.util.Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        userService.updateCurrentUser("user-a", "user-a", "a@example.com");

        assertThat(existing.getTokenVersion()).isZero();
        verify(userRepository).save(existing);
    }

    @Test
    void shouldRunProfileUpdateInsideTransaction() throws NoSuchMethodException {
        assertThat(UserServiceImpl.class
                .getMethod("updateCurrentUser", String.class, String.class, String.class)
                .isAnnotationPresent(Transactional.class))
                .isTrue();
    }

    @Test
    void shouldChangePasswordOnlyAfterMatchingCurrentPassword() {
        User existing = new User("user-a", "a@example.com", "old-password");
        when(userRepository.findByUsernameForUpdate("user-a"))
                .thenReturn(java.util.Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        userService.changePassword("user-a", "old-password", "new-password");

        assertThat(new BCryptPasswordEncoder().matches(
                "new-password",
                existing.getPassword()
        )).isTrue();
        assertThat(existing.getTokenVersion()).isEqualTo(1L);
        verify(userRepository).save(existing);
    }
}
