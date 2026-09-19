package com.bookstore.dto.auth;

import com.bookstore.dto.user.UserDto;

public record AuthResponse(
        String token,
        String tokenType,
        UserDto user
) {
    public AuthResponse(String token, UserDto user) {
        this(token, "Bearer", user);
    }

    @Override
    public String toString() {
        return "AuthResponse[token=[PROTECTED], tokenType=" + tokenType + ", user=" + user + "]";
    }
}
