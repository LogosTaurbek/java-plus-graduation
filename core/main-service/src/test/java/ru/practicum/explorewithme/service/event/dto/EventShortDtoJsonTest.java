// EventShortDtoJsonTest.java
package ru.practicum.explorewithme.service.event.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class EventShortDtoJsonTest {

    @SuppressWarnings("unused")
    @Autowired
    private JacksonTester<EventShortDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        var dto = EventShortDto.builder()
                .id(1L)
                .annotation("Short annotation")
                .category(new CategoryDto(2L, "Концерты"))
                .confirmedRequests(3L)
                .eventDate("2024-02-01 12:00:00")
                .initiator(new UserShortDto(3L, "Иван Иванов"))
                .paid(false)
                .title("Short title")
                .views(10L)
                .build();

        var json = jacksonTester.write(dto);
        assertThat(json).hasJsonPathStringValue("$.title");
        assertThat(json).extractingJsonPathNumberValue("$.views").isEqualTo(10);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\n"
                + "    \"id\": 1,\n"
                + "    \"annotation\": \"Short annotation\",\n"
                + "    \"category\": { \"id\": 2, \"name\": \"Концерты\" },\n"
                + "    \"confirmedRequests\": 3,\n"
                + "    \"eventDate\": \"2024-02-01 12:00:00\",\n"
                + "    \"initiator\": { \"id\": 3, \"name\": \"Иван Иванов\" },\n"
                + "    \"paid\": false,\n"
                + "    \"title\": \"Short title\",\n"
                + "    \"views\": 10\n"
                + "}";
        var dto = jacksonTester.parse(content).getObject();
        assertThat(dto.getTitle()).isEqualTo("Short title");
        assertThat(dto.getPaid()).isFalse();
    }
}
