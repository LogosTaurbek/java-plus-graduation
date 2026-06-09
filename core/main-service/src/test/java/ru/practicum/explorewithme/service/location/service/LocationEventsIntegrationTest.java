package ru.practicum.explorewithme.service.location.service;

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
import ru.practicum.explorewithme.service.event.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.service.event.enums.AdminEventStateAction;
import ru.practicum.explorewithme.service.event.service.EventService;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.dto.UserDto;
import ru.practicum.explorewithme.service.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class LocationEventsIntegrationTest {

    @Autowired
    private EventService eventService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long locId;
    private Long catId;
    private Long userId;

    @BeforeEach
    void setUp() {
        CategoryDto cat = categoryService.createCategory(new NewCategoryRequest("LocationEventTest"));
        catId = cat.getId();

        UserDto user = userService.registerUser(new NewUserRequest("locevt@test.com", "LocEvtUser"));
        userId = user.getId();

        ru.practicum.explorewithme.service.location.dto.LocationDto moscowLoc = locationService.createLocation(
                NewLocationRequest.builder()
                        .name("Moscow Center")
                        .lat(55.75f)
                        .lon(37.62f)
                        .radius(50f)
                        .build());
        locId = moscowLoc.getId();

        Long id1 = eventService.addEvent(userId, buildEventDto("Inside 1", 55.80f, 37.70f)).getId();
        Long id2 = eventService.addEvent(userId, buildEventDto("Inside 2", 55.70f, 37.55f)).getId();
        Long idOut = eventService.addEvent(userId, buildEventDto("Outside SPb", 59.95f, 30.32f)).getId();
        eventService.addEvent(userId, buildEventDto("Pending Inside", 55.78f, 37.65f));

        publish(id1);
        publish(id2);
        publish(idOut);
    }

    @Test
    void getEventsByLocation_returnsOnlyPublishedInsideRadius() {
        List<EventFullDto> result = eventService.getEventsByLocation(locId, 0, 10);

        assertThat(result).hasSize(2);
        List<String> titles = result.stream().map(EventFullDto::getTitle).toList();
        assertThat(titles).containsExactlyInAnyOrder("Inside 1", "Inside 2");
    }

    @Test
    void getEventsByLocation_emptyWhenNoEventsInLocation() {
        ru.practicum.explorewithme.service.location.dto.LocationDto antarctic = locationService.createLocation(
                NewLocationRequest.builder()
                        .name("Antarctica")
                        .lat(-90f)
                        .lon(0f)
                        .radius(1f)
                        .build());

        List<EventFullDto> result = eventService.getEventsByLocation(antarctic.getId(), 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getEventsByLocation_throwsNotFound_whenLocationMissing() {
        assertThatThrownBy(() -> eventService.getEventsByLocation(99999L, 0, 10))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getEventsByLocation_pagination() {
        List<EventFullDto> result = eventService.getEventsByLocation(locId, 0, 1);

        assertThat(result).hasSize(1);
    }

    private NewEventDto buildEventDto(String title, float lat, float lon) {
        return NewEventDto.builder()
                .annotation("Annotation for " + title + " that is long enough")
                .category(catId)
                .description("Description for " + title + " that is long enough")
                .eventDate(LocalDateTime.now().plusDays(1).format(fmt))
                .location(new LocationDto(lat, lon))
                .paid(false)
                .participantLimit(0)
                .requestModeration(false)
                .title(title)
                .build();
    }

    private void publish(Long eventId) {
        eventService.updateEventByAdmin(eventId,
                UpdateEventAdminRequest.builder()
                        .stateAction(AdminEventStateAction.PUBLISH_EVENT)
                        .build());
    }
}
