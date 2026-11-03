package com.eva.evagita.controller;

import com.eva.evagita.exception.GlobalExceptionHandler;
import com.eva.evagita.model.User;
import com.eva.evagita.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetCurrentUserFromPrincipal() throws Exception {
        User user = new User("testuser", "test@example.com", "hashed-password");
        when(userService.getCurrentUser("testuser")).thenReturn(user);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(userService).getCurrentUser("testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUpdateOnlyCurrentUserFromPrincipal() throws Exception {
        User updatedUser = new User(
                "updated-user", "updated@example.com", "hashed-password");
        when(userService.updateCurrentUser(
                "testuser", "updated-user", "updated@example.com"
        )).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "updated-user",
                                  "email": "updated@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated-user"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userService).updateCurrentUser(
                "testuser", "updated-user", "updated@example.com");
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldChangePasswordForCurrentPrincipalOnly() throws Exception {
        doNothing().when(userService).changePassword(
                "testuser", "current-password", "new-password");

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "current-password",
                                  "newPassword": "new-password"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(
                "testuser", "current-password", "new-password");
    }

    @Test
    void shouldKeepRegistrationPublicAndNeverReturnPassword() throws Exception {
        User user = new User("newuser", "new@example.com", "hashed-password");
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "newuser",
                                  "email": "new@example.com",
                                  "password": "registration-password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldNotExposeArbitraryUserRoutes() throws Exception {
        mockMvc.perform(get("/api/users/42"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/users/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}
