package ru.practicum.explorewithme.service.event.client;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component("eventRequestClientFallback")
public class RequestClientFallback implements RequestClient {

    @Override
    public Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds) {
        return Collections.emptyMap();
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return Collections.emptyList();
    }
}
