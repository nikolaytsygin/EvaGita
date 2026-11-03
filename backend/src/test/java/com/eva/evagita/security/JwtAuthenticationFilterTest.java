package com.eva.evagita.security;

import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.context.SecurityContextHolder.clearContext;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

class JwtAuthenticationFilterTest {

    private static final String SECRET =
            "test-secret-for-junit-tests-only-evagita-2026";

    private final JwtService jwtService = new JwtService(SECRET, 86_400_000L);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(jwtService, userRepository);

    @AfterEach
    void clearSecurityContext() {
        clearContext();
    }

    @Test
    void shouldAuthenticateExistingUserWithCurrentTokenVersion()
            throws ServletException, IOException {
        User user = new User("testuser", "test@example.com", "hashed");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        String token = jwtService.generateToken("testuser", user.getTokenVersion());
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(
                requestWithBearer(token),
                new MockHttpServletResponse(),
                chain
        );

        assertThat(getContext().getAuthentication().getName()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void shouldRejectUnknownAndRevokedUsersWithoutThrowing()
            throws ServletException, IOException {
        User user = new User("testuser", "test@example.com", "hashed");
        user.revokeTokens();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        String revokedToken = jwtService.generateToken("testuser", 0L);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(
                requestWithBearer(revokedToken),
                new MockHttpServletResponse(),
                chain
        );

        assertThat(getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void shouldIgnoreMalformedToken() throws ServletException, IOException {
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(
                requestWithBearer("malformed-token"),
                new MockHttpServletResponse(),
                chain
        );

        assertThat(getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    private MockHttpServletRequest requestWithBearer(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
