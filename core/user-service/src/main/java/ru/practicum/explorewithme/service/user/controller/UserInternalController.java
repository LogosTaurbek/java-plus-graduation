package ru.practicum.explorewithme.service.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;
import ru.practicum.explorewithme.service.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public UserShortDto getUser(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserShortDto> getUsers(@RequestParam List<Long> ids) {
        return userService.getUsersByIds(ids);
    }
}
