package ru.practicum.explorewithme.service.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.service.CategoryService;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.dto.EventSearchParamsAdmin;
import ru.practicum.explorewithme.service.event.dto.LocationDto;
import ru.practicum.explorewithme.service.event.dto.NewEventDto;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.dto.UserDto;
import ru.practicum.explorewithme.service.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class EventServiceAdminIntegrationTest {

    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long catId;
    private UserDto user1;
    private UserDto user2;

    @BeforeEach
    void setUp() {
        CategoryDto cat = categoryService.createCategory(new NewCategoryRequest("Category Integration"));
        catId = cat.getId();
        user1 = userService.registerUser(new NewUserRequest("user1@test.com", "User1"));
        user2 = userService.registerUser(new NewUserRequest("user2@test.com", "User2"));
    }

    @Test
    void getEventsByAdmin_FilterByUsers() {
        eventService.addEvent(user1.getId(), buildDto("Event 1", LocalDateTime.now().plusDays(1)));
        eventService.addEvent(user2.getId(), buildDto("Event 2", LocalDateTime.now().plusDays(2)));

        EventSearchParamsAdmin params = new EventSearchParamsAdmin(List.of(user1.getId()), null, null, null, null, 0, 10);
        List<EventFullDto> result = eventService.getEventsByAdmin(params);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInitiator().getId()).isEqualTo(user1.getId());
    }

    @Test
    void getEventsByAdmin_FilterByStates() {
        eventService.addEvent(user1.getId(), buildDto("Event 1", LocalDateTime.now().plusDays(1)));
EventSearchParamsAdmin params = new EventSearchParamsAdmin(
        null, List.of(EventState.PENDING), null, null, null, 0, 10);
        List<EventFullDto> result = eventService.getEventsByAdmin(params);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getState()).isEqualTo(EventState.PENDING);
    }

    @Test
    void getEventsByAdmin_FilterByDateRange() {
        LocalDateTime now = LocalDateTime.now();
        eventService.addEvent(user1.getId(), buildDto("Event 1", now.plusDays(1)));
        eventService.addEvent(user1.getId(), buildDto("Event 2", now.plusDays(5)));

        List<EventFullDto> result = eventService.getEventsByAdmin(new EventSearchParamsAdmin(
                null, null, null, now.plusDays(2), now.plusDays(6), 0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Event 2");
    }

    private NewEventDto buildDto(String title, LocalDateTime eventDate) {
        return NewEventDto.builder()
                .annotation("Annotation for " + title + " that is long enough")
                .category(catId)
                .description("Description for " + title + " that is long enough")
                .eventDate(eventDate.format(fmt))
                .location(new LocationDto(55.0f, 37.0f))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .title(title)
                .build();
    }
}
