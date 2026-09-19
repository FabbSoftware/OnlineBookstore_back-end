package com.bookstore.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.lang.NonNull;

import java.util.Arrays;

public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotEmpty(message = "Password is required")
        char[] password
) {
    public LoginRequest(String email, String password) {
        this(email, password != null ? password.toCharArray() : null);
    }

    public void erasePassword() {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }

    @Override
    @NonNull
    public String toString() {
        return "LoginRequest[email=" + email + ", password=[PROTECTED]]";
    }
}
