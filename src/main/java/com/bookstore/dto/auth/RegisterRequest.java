package com.bookstore.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Arrays;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotEmpty(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        char[] password,

        @NotBlank(message = "Full name is required")
        String fullName
) {
    public RegisterRequest(String email, String password, String fullName) {
        this(email, password != null ? password.toCharArray() : null, fullName);
    }

    public void erasePassword() {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }

    @Override
    public String toString() {
        return "RegisterRequest[email=" + email + ", fullName=" + fullName + ", password=[PROTECTED]]";
    }
}
