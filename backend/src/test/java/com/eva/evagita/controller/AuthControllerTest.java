package com.eva.evagita.controller;

import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import com.eva.evagita.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
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
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getPassword()).thenReturn("password");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken("testuser"))
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

        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken("testuser");
    }

    @Test
    void shouldReturn401WhenPasswordIsIncorrect() throws Exception {
        User user = new User(
                "testuser",
                "test@example.com",
                "password"
        );

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

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

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void shouldReturn401WhenUserDoesNotExist() throws Exception {
        when(userRepository.findByEmail("unknown@example.com"))
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

        verify(jwtService, never()).generateToken(anyString());
    }
}
