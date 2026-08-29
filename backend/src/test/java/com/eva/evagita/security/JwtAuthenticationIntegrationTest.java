package com.eva.evagita.security;

import com.eva.evagita.PostgresIntegrationTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class JwtAuthenticationIntegrationTest extends PostgresIntegrationTest {

    private static final String SECRET =
            "EvaGitaJwtSecretKeyForDevelopmentOnly_ChangeThisToAStrongSecretKey_2026";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldReturn401ForExpiredJwt() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        Date now = new Date();

        String expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(now.getTime() - 2000))
                .expiration(new Date(now.getTime() - 1000))
                .signWith(key)
                .compact();

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForInvalidJwt() throws Exception {
        String invalidToken = "this.is.not.a.valid.jwt";

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }
}
