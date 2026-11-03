package com.eva.evagita.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHashServiceTest {

    private PasswordHashService passwordHashService;

    @BeforeEach
    void setUp() {
        passwordHashService = new PasswordHashService(
                new BCryptPasswordEncoder()
        );
    }

    @Test
    void shouldHashPlaintextPassword() {
        String hash = passwordHashService.encodeIfNeeded("password");

        assertNotEquals("password", hash);
        assertTrue(passwordHashService.isBcryptHash(hash));
        assertTrue(passwordHashService.matches("password", hash));
    }

    @Test
    void shouldNotHashBcryptValueAgain() {
        String hash = passwordHashService.encodeIfNeeded("password");

        assertTrue(passwordHashService.isBcryptHash(hash));
        assertTrue(passwordHashService.encodeIfNeeded(hash).equals(hash));
    }

    @Test
    void shouldMatchLegacyPlaintextForMigration() {
        assertTrue(passwordHashService.matches("legacy", "legacy"));
        assertFalse(passwordHashService.matches("wrong", "legacy"));
    }
}
