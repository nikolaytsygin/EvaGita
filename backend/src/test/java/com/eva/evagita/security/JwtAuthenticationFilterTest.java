package com.eva.evagita.security;

import com.eva.evagita.model.User;
import com.eva.evagita.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        when(user.getUsername()).thenReturn("testuser");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        String token = jwtService.generateToken(user);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(
                requestWithBearer(token),
                new MockHttpServletResponse(),
                chain
        );

        assertThat(getContext().getAuthentication().getName()).isEqualTo("testuser");
        verify(userRepository).findById(42L);
    }

    @Test
    void shouldRejectUnknownAndRevokedUsersWithoutThrowing()
            throws ServletException, IOException {
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getTokenVersion()).thenReturn(1L);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        String revokedToken = signedToken("testuser", 42L, 0L);
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
    void shouldRejectUnknownUserId() throws ServletException, IOException {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        filter.doFilter(
                requestWithBearer(signedToken("testuser", 999L, 0L)),
                new MockHttpServletResponse(),
                new MockFilterChain()
        );

        assertThat(getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldRejectTokenWithoutUserId()
            throws ServletException, IOException {
        String legacyToken = signedToken("testuser", null, 0L);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(
                requestWithBearer(legacyToken),
                new MockHttpServletResponse(),
                chain
        );

        assertThat(getContext().getAuthentication()).isNull();
        org.mockito.Mockito.verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectTokenWithNonNumericUserId() throws ServletException, IOException {
        String token = signedToken("testuser", "not-a-number", 0L);

        filter.doFilter(
                requestWithBearer(token),
                new MockHttpServletResponse(),
                new MockFilterChain()
        );

        assertThat(getContext().getAuthentication()).isNull();
        org.mockito.Mockito.verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectTokenWithMismatchedUsername() throws ServletException, IOException {
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        when(user.getUsername()).thenReturn("actual-user");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        String token = signedToken("other-user", 42L, 0L);

        filter.doFilter(requestWithBearer(token), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldRejectTokenWithStaleTokenVersion() throws ServletException, IOException {
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getTokenVersion()).thenReturn(2L);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        String token = signedToken("testuser", 42L, 1L);

        filter.doFilter(requestWithBearer(token), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldPropagateRepositoryFailureInsteadOfTreatingItAsJwtFailure()
            throws ServletException, IOException {
        when(userRepository.findById(42L)).thenThrow(new IllegalStateException("database unavailable"));
        String token = signedToken("testuser", 42L, 0L);

        assertThatThrownBy(() -> filter.doFilter(
                requestWithBearer(token),
                new MockHttpServletResponse(),
                new MockFilterChain()
        )).isInstanceOf(IllegalStateException.class)
                .hasMessage("database unavailable");
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

    private String signedToken(String username, Object userId, long tokenVersion) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        var builder = Jwts.builder()
                .subject(username)
                .claim("token_version", tokenVersion)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86_400_000L));
        if (userId != null) {
            builder.claim("user_id", userId);
        }
        return builder.signWith(key).compact();
    }
}
