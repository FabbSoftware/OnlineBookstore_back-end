package com.bookstore.dto.user;

import com.bookstore.domain.Role;

import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String fullName,
        Role role
) {}
