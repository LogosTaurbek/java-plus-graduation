// EventFullDtoJsonTest.java
package ru.practicum.explorewithme.service.event.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class EventFullDtoJsonTest {

    @SuppressWarnings("unused")
    @Autowired
    private JacksonTester<EventFullDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        var dto = EventFullDto.builder()
                .id(1L)
                .annotation("Test annotation")
                .category(new CategoryDto(2L, "Концерты"))
                .confirmedRequests(5L)
                .createdOn("2024-01-01 10:00:00")
                .description("Test description")
                .eventDate("2024-02-01 12:00:00")
                .initiator(new UserShortDto(3L, "Иван Иванов"))
                .location(new LocationDto(55.754167f, 37.62f))
                .paid(true)
                .participantLimit(10)
                .publishedOn("2024-01-02 10:00:00")
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .title("Test title")
                .views(15L)
                .build();

        var json = jacksonTester.write(dto);
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathStringValue("$.annotation");
        assertThat(json).hasJsonPathStringValue("$.eventDate");
        assertThat(json).extractingJsonPathNumberValue("$.views").isEqualTo(15);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\n"
                + "    \"id\": 1,\n"
                + "    \"annotation\": \"Test annotation\",\n"
                + "    \"category\": { \"id\": 2, \"name\": \"Концерты\" },\n"
                + "    \"confirmedRequests\": 5,\n"
                + "    \"createdOn\": \"2024-01-01 10:00:00\",\n"
                + "    \"description\": \"Test description\",\n"
                + "    \"eventDate\": \"2024-02-01 12:00:00\",\n"
                + "    \"initiator\": { \"id\": 3, \"name\": \"Иван Иванов\" },\n"
                + "    \"location\": { \"lat\": 55.754167, \"lon\": 37.62 },\n"
                + "    \"paid\": true,\n"
                + "    \"participantLimit\": 10,\n"
                + "    \"publishedOn\": \"2024-01-02 10:00:00\",\n"
                + "    \"requestModeration\": true,\n"
                + "    \"state\": \"PUBLISHED\",\n"
                + "    \"title\": \"Test title\",\n"
                + "    \"views\": 15\n"
                + "}";
        var dto = jacksonTester.parse(content).getObject();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getState()).isEqualTo(EventState.PUBLISHED);
    }
}
