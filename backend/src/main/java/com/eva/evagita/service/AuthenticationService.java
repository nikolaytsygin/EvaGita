package com.eva.evagita.service;

import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.security.PasswordHashService;
import com.eva.evagita.security.LoginRateLimiter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHashService passwordHashService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordHashService passwordHashService
    ) {
        this.userRepository = userRepository;
        this.passwordHashService = passwordHashService;
    }

    @Transactional
    public Optional<User> authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmailForUpdate(
                LoginRateLimiter.normalizeEmail(email)
        ).orElse(null);

        if (user == null
                || !passwordHashService.matches(
                        rawPassword,
                        user.getPassword()
                )) {
            return Optional.empty();
        }

        if (!passwordHashService.isBcryptHash(user.getPassword())) {
            user.setPassword(
                    passwordHashService.encodeIfNeeded(rawPassword)
            );
            userRepository.save(user);
        }

        return Optional.of(user);
    }

    @Transactional
    public void revokeTokens(String username) {
        User user = userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.revokeTokens();
        userRepository.save(user);
    }
}
