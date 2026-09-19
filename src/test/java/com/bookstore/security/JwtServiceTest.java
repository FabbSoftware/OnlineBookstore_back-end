package com.bookstore.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        // 256-bit test secret in base64
        String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        properties.setSecret(SECRET);
        // 1 hour
        long EXPIRATION_MS = 3600000;
        properties.setExpirationMs(EXPIRATION_MS);
        jwtService = new JwtService(properties);
    }

    @Test
    void shouldGenerateValidTokenAndExtractUsername() {
        UserDetails userDetails = new User("alice@example.com", "password", Collections.emptyList());

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void shouldReturnFalseForDifferentUser() {
        UserDetails userAlice = new User("alice@example.com", "password", Collections.emptyList());
        UserDetails userBob = new User("bob@example.com", "password", Collections.emptyList());

        String token = jwtService.generateToken(userAlice);

        assertThat(jwtService.isTokenValid(token, userBob)).isFalse();
    }
}
