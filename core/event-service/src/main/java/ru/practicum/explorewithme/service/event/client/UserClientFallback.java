package ru.practicum.explorewithme.service.event.client;

import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

import java.util.Collections;
import java.util.List;

@Component("eventUserClientFallback")
public class UserClientFallback implements UserClient {

    @Override
    public UserShortDto getUserById(Long userId) {
        return new UserShortDto(userId, "N/A");
    }

    @Override
    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        return Collections.emptyList();
    }
}
