package com.eva.evagita.controller;

import com.eva.evagita.model.User;
import com.eva.evagita.security.JwtService;
import com.eva.evagita.security.LoginRateLimiter;
import com.eva.evagita.security.SmartCaptchaProperties;
import com.eva.evagita.security.SmartCaptchaVerificationStatus;
import com.eva.evagita.security.SmartCaptchaVerifier;
import com.eva.evagita.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private LoginRateLimiter loginRateLimiter;

    @MockitoBean
    private SmartCaptchaProperties smartCaptchaProperties;

    @MockitoBean
    private SmartCaptchaVerifier smartCaptchaVerifier;

    @BeforeEach
    void allowLoginAttemptByDefault() {
        when(loginRateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getEmail()).thenReturn("test@example.com");
        when(authenticationService.authenticate("test@example.com", "password"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("valid.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("valid.jwt.token"));

        verify(authenticationService)
                .authenticate("test@example.com", "password");
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldReturn401WhenPasswordIsIncorrect() throws Exception {
        when(authenticationService.authenticate("test@example.com", "wrong-password"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));

        verify(jwtService, never()).generateToken(any(User.class));
        verify(loginRateLimiter).recordFailure("test@example.com", "127.0.0.1");
    }

    @Test
    void shouldReturn401WhenUserDoesNotExist() throws Exception {
        when(authenticationService.authenticate("unknown@example.com", "password"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "unknown@example.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));

        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void shouldUpgradeLegacyPasswordAfterSuccessfulLogin() throws Exception {
        User user = mock(User.class);
        when(user.getId()).thenReturn(2L);
        when(user.getUsername()).thenReturn("legacy-user");
        when(user.getEmail()).thenReturn("legacy@example.com");
        when(authenticationService.authenticate("legacy@example.com", "legacy-password"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user))
                .thenReturn("valid.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "legacy@example.com",
                                  "password": "legacy-password"
                                }
                                """))
                .andExpect(status().isOk());

        verify(authenticationService)
                .authenticate("legacy@example.com", "legacy-password");
    }

    @Test
    void shouldReturn400ForInvalidLoginPayload() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPasswordIsTooLong() throws Exception {
        String password = "a".repeat(129);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "%s"
                                }
                                """.formatted(password)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn429WhenLoginIsBlocked() throws Exception {
        when(loginRateLimiter.tryAcquire("test@example.com", "127.0.0.1"))
                .thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "TEST@example.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Too many login attempts"));

        verify(authenticationService, never()).authenticate(anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldLogoutByRevokingCurrentUserTokens() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent());

        verify(authenticationService).revokeTokens("testuser");
    }

    @Test
    void shouldRequireCaptchaAfterEmailSoftThreshold() throws Exception {
        when(smartCaptchaProperties.isEnabled()).thenReturn(true);
        when(loginRateLimiter.requiresCaptcha("test@example.com"))
                .thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isPreconditionRequired())
                .andExpect(jsonPath("$.code").value("CAPTCHA_REQUIRED"));

        verify(authenticationService, never()).authenticate(anyString(), anyString());
    }

    @Test
    void shouldContinueLoginAfterCaptchaVerification() throws Exception {
        User user = mock(User.class);
        when(smartCaptchaProperties.isEnabled()).thenReturn(true);
        when(loginRateLimiter.requiresCaptcha("test@example.com")).thenReturn(true);
        when(smartCaptchaVerifier.verify("captcha-token", "127.0.0.1"))
                .thenReturn(SmartCaptchaVerificationStatus.VERIFIED);
        when(authenticationService.authenticate("test@example.com", "password"))
                .thenReturn(Optional.of(user));
        when(user.getUsername()).thenReturn("testuser");
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getId()).thenReturn(1L);
        when(jwtService.generateToken(user)).thenReturn("token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "password",
                                  "captchaToken": "captcha-token"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidCaptchaWithoutAuthenticating() throws Exception {
        when(smartCaptchaProperties.isEnabled()).thenReturn(true);
        when(loginRateLimiter.requiresCaptcha("test@example.com")).thenReturn(true);
        when(smartCaptchaVerifier.verify("captcha-token", "127.0.0.1"))
                .thenReturn(SmartCaptchaVerificationStatus.REJECTED);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "password",
                                  "captchaToken": "captcha-token"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CAPTCHA_INVALID"));

        verify(authenticationService, never()).authenticate(anyString(), anyString());
    }
}
