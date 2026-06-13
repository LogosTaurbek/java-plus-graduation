package ru.practicum.explorewithme.service.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service", contextId = "eventUserClient", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/internal/users/{userId}")
    UserShortDto getUserById(@PathVariable Long userId);

    @GetMapping("/internal/users")
    List<UserShortDto> getUsersByIds(@RequestParam List<Long> ids);
}
