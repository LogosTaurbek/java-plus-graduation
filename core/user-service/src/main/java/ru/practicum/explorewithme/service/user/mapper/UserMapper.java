package ru.practicum.explorewithme.service.user.mapper;

import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.dto.UserDto;
import ru.practicum.explorewithme.service.user.model.User;

public final class UserMapper {
    public static User toEntity(NewUserRequest request) {
        return new User(null, request.getEmail(), request.getName());
    }

    public static UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }
}