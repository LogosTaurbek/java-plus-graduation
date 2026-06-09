package ru.practicum.explorewithme.service.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.service.CategoryService;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.dto.LocationDto;
import ru.practicum.explorewithme.service.event.dto.NewEventDto;
import ru.practicum.explorewithme.service.event.service.EventService;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class EventRequestServiceIntegrationTest {

    @Autowired
    private EventRequestService requestService;
    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Long categoryId;

    @BeforeEach
    void createCategory() {
        CategoryDto cat = categoryService.createCategory(new NewCategoryRequest("Тестовая категория"));
        categoryId = cat.getId();
    }

    @Test
    void shouldGetEmptyRequestsForNewEvent() {
        var initiator = userService.registerUser(new NewUserRequest("init@example.com", "Init"));

        NewEventDto dto = NewEventDto.builder()
                .annotation("Valid annotation for testing")
                .category(categoryId)
                .description("Valid description for testing")
                .eventDate(LocalDateTime.now().plusDays(2).format(fmt))
                .location(new LocationDto(55.75f, 37.62f))
                .requestModeration(true)
                .participantLimit(1)
                .title("Event for requests")
                .build();
        EventFullDto event = eventService.addEvent(initiator.getId(), dto);

        List<ParticipationRequestDto> requests = requestService.getEventRequests(initiator.getId(), event.getId());
        assertThat(requests).isEmpty();
    }
}
