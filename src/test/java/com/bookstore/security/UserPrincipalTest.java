package com.bookstore.security;

import com.bookstore.domain.Role;
import com.bookstore.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void fromUserForJwtShouldHaveNullPassword() {
        User user = new User(UUID.randomUUID(), "test@example.com", "bcryptHash123", "Test User", Role.ROLE_USER);

        UserPrincipal principal = UserPrincipal.fromUserForJwt(user);

        assertThat(principal.getPassword()).isNull();
        assertThat(principal.getUsername()).isEqualTo("test@example.com");
        assertThat(principal.getAuthorities()).hasSize(1);
    }

    @Test
    void fromUserForAuthenticationShouldRetainAndErasePassword() {
        User user = new User(UUID.randomUUID(), "test@example.com", "bcryptHash123", "Test User", Role.ROLE_USER);

        UserPrincipal principal = UserPrincipal.fromUserForAuthentication(user);

        assertThat(principal.getPassword()).isEqualTo("bcryptHash123");

        principal.eraseCredentials();

        assertThat(principal.getPassword()).isNull();
    }

    @Test
    void jacksonSerializationShouldOmitPasswordForUserAndPrincipal() throws Exception {
        User user = new User(UUID.randomUUID(), "test@example.com", "sensitivePasswordHash", "Test User", Role.ROLE_USER);
        UserPrincipal principal = UserPrincipal.fromUserForAuthentication(user);

        String userJson = objectMapper.writeValueAsString(user);
        String principalJson = objectMapper.writeValueAsString(principal);

        assertThat(userJson).doesNotContain("sensitivePasswordHash");
        assertThat(userJson).doesNotContain("password");
        assertThat(principalJson).doesNotContain("sensitivePasswordHash");
        assertThat(principalJson).doesNotContain("password");
    }

    @Test
    void toStringShouldExcludePassword() {
        User user = new User(UUID.randomUUID(), "test@example.com", "sensitivePasswordHash", "Test User", Role.ROLE_USER);
        UserPrincipal principal = UserPrincipal.fromUserForAuthentication(user);

        assertThat(user.toString()).doesNotContain("sensitivePasswordHash");
        assertThat(principal.toString()).doesNotContain("sensitivePasswordHash");
    }
}
