package com.eva.evagita.service;

import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.security.PasswordHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    private UserRepository userRepository;
    private PasswordHashService passwordHashService;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHashService = new PasswordHashService(
                new BCryptPasswordEncoder()
        );
        authenticationService = new AuthenticationService(
                userRepository,
                passwordHashService
        );
    }

    @Test
    void shouldUpgradeLegacyPasswordAfterSuccessfulAuthentication() {
        User user = new User("legacy", "legacy@example.com", "legacy-password");
        when(userRepository.findByEmailForUpdate("legacy@example.com"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        Optional<User> authenticated = authenticationService.authenticate(
                "legacy@example.com",
                "legacy-password"
        );

        assertThat(authenticated).containsSame(user);
        assertThat(passwordHashService.matches(
                "legacy-password",
                user.getPassword()
        )).isTrue();
        assertThat(passwordHashService.isBcryptHash(user.getPassword()))
                .isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void shouldNotSaveWhenBcryptPasswordMatches() {
        String hash = passwordHashService.encodeIfNeeded("password");
        User user = new User("user", "user@example.com", hash);
        when(userRepository.findByEmailForUpdate("user@example.com"))
                .thenReturn(Optional.of(user));

        assertThat(authenticationService.authenticate(
                "user@example.com",
                "password"
        )).containsSame(user);
        verify(userRepository, never()).save(user);
    }

    @Test
    void shouldRevokeAllTokensForUser() {
        User user = new User("user", "user@example.com", "hash");
        when(userRepository.findByUsernameForUpdate("user"))
                .thenReturn(Optional.of(user));

        authenticationService.revokeTokens("user");

        assertThat(user.getTokenVersion()).isEqualTo(1L);
        verify(userRepository).save(user);
    }
}
