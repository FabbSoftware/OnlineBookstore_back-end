package com.bookstore.dto.user;

import com.bookstore.domain.Role;

public record UserDto(
        Long id,
        String email,
        String fullName,
        Role role
) {}
