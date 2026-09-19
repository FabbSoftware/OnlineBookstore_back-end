package com.bookstore.service;

import com.bookstore.dto.auth.AuthResponse;
import com.bookstore.dto.auth.LoginRequest;
import com.bookstore.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AuthIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    void shouldRegisterAndThenLoginSuccessfully() {
        RegisterRequest registerRequest = new RegisterRequest("newuser@example.com", "securePassword123", "New User");
        AuthResponse registerResponse = authService.register(registerRequest);

        assertThat(registerResponse).isNotNull();
        assertThat(registerResponse.token()).isNotBlank();
        assertThat(registerResponse.user().email()).isEqualTo("newuser@example.com");

        LoginRequest loginRequest = new LoginRequest("newuser@example.com", "securePassword123");
        AuthResponse loginResponse = authService.login(loginRequest);

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.token()).isNotBlank();
        assertThat(loginResponse.user().email()).isEqualTo("newuser@example.com");
    }
}
