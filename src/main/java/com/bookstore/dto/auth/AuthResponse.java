package com.bookstore.dto.auth;

import com.bookstore.dto.user.UserDto;
import org.springframework.lang.NonNull;

public record AuthResponse(
        String token,
        String tokenType,
        UserDto user
) {
    public AuthResponse(String token, UserDto user) {
        this(token, "Bearer", user);
    }

    @Override
    @NonNull
    public String toString() {
        return "AuthResponse[token=[PROTECTED], tokenType=" + tokenType + ", user=" + user + "]";
    }
}
