package ru.practicum.explorewithme.service.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

@FeignClient(name = "user-service", contextId = "requestUserClient", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/internal/users/{userId}")
    UserShortDto getUserById(@PathVariable Long userId);
}
