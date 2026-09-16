package com.bookstore.mapper;

import com.bookstore.domain.User;
import com.bookstore.dto.user.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
