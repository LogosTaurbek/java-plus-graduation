package ru.practicum.explorewithme.service.user.service;

import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.dto.UserDto;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

import java.util.List;

public interface UserService {
    UserDto registerUser(NewUserRequest newUserRequest);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void deleteUser(Long userId);

    UserShortDto getUserById(Long userId);

    List<UserShortDto> getUsersByIds(List<Long> ids);
}