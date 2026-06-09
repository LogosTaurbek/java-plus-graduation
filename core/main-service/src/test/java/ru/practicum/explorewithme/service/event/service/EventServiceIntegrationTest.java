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
import ru.practicum.explorewithme.service.event.dto.LocationDto;
import ru.practicum.explorewithme.service.event.dto.NewEventDto;
import ru.practicum.explorewithme.service.event.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.enums.UserEventStateAction;
import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class EventServiceIntegrationTest {

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
    void fullLifecycleTest() {
        var user = userService.registerUser(new NewUserRequest("creator@example.com", "Creator"));

        NewEventDto newEvent = buildValidDto(categoryId, LocalDateTime.now().plusDays(3));
        EventFullDto created = eventService.addEvent(user.getId(), newEvent);
        assertThat(created.getState()).isEqualTo(EventState.PENDING);

        // обновление заголовка
        UpdateEventUserRequest update = UpdateEventUserRequest.builder().title("Updated title").build();
        EventFullDto updated = eventService.updateEvent(user.getId(), created.getId(), update);
        assertThat(updated.getTitle()).isEqualTo("Updated title");

        // отмена
        update = UpdateEventUserRequest.builder().stateAction(UserEventStateAction.CANCEL_REVIEW).build();
        EventFullDto canceled = eventService.updateEvent(user.getId(), created.getId(), update);
        assertThat(canceled.getState()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void getEvents_Pagination() {
        var user = userService.registerUser(new NewUserRequest("pagin@example.com", "Pagin"));
        for (int i = 0; i < 5; i++) {
            eventService.addEvent(user.getId(), buildValidDto(categoryId, LocalDateTime.now().plusDays(2 + i)));
        }
        var page1 = eventService.getEvents(user.getId(), 0, 2);
        assertThat(page1).hasSizeLessThanOrEqualTo(2);
    }

    private NewEventDto buildValidDto(Long categoryId, LocalDateTime eventDate) {
        return NewEventDto.builder()
                .annotation("Valid annotation for testing")
                .category(categoryId)
                .description("Valid description for testing")
                .eventDate(eventDate.format(fmt))
                .location(new LocationDto(55.75f, 37.62f))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .title("Test event")
                .build();
    }
}