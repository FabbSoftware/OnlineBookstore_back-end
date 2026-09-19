package com.bookstore.dto.auth;

import com.bookstore.domain.Role;
import com.bookstore.dto.user.UserDto;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DtoSecurityTest {

    @Test
    void loginRequestShouldMaskPasswordInToStringAndErasePassword() {
        char[] password = "mySecretPassword".toCharArray();
        LoginRequest request = new LoginRequest("user@example.com", password);

        assertThat(request.toString()).doesNotContain("mySecretPassword");
        assertThat(request.toString()).contains("[PROTECTED]");

        request.erasePassword();

        for (char c : password) {
            assertThat(c).isEqualTo('\0');
        }
    }

    @Test
    void registerRequestShouldMaskPasswordInToStringAndErasePassword() {
        char[] password = "mySecretPassword".toCharArray();
        RegisterRequest request = new RegisterRequest("user@example.com", password, "Jane Doe");

        assertThat(request.toString()).doesNotContain("mySecretPassword");
        assertThat(request.toString()).contains("[PROTECTED]");

        request.erasePassword();

        for (char c : password) {
            assertThat(c).isEqualTo('\0');
        }
    }

    @Test
    void authResponseShouldMaskTokenInToString() {
        UserDto userDto = new UserDto(UUID.randomUUID(), "user@example.com", "Jane Doe", Role.ROLE_USER);
        AuthResponse response = new AuthResponse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.sensitivePayload", userDto);

        assertThat(response.toString()).doesNotContain("eyJhbGciOi");
        assertThat(response.toString()).contains("[PROTECTED]");
    }
}
