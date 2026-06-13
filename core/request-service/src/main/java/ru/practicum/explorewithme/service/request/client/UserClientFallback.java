package ru.practicum.explorewithme.service.request.client;

import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

@Component("requestUserClientFallback")
public class UserClientFallback implements UserClient {

    @Override
    public UserShortDto getUserById(Long userId) {
        return null;
    }
}
