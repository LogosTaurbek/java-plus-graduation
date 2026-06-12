// NewEventDtoJsonTest.java
package ru.practicum.explorewithme.service.event.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class NewEventDtoJsonTest {

    @SuppressWarnings("unused")
    @Autowired
    private JacksonTester<NewEventDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        var dto = NewEventDto.builder()
                .annotation("Valid annotation for testing")
                .category(1L)
                .description("Valid description for testing")
                .eventDate("2026-05-01 12:00:00")
                .location(new LocationDto(55.75f, 37.62f))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .title("Valid title")
                .build();

        var json = jacksonTester.write(dto);
        assertThat(json).hasJsonPathStringValue("$.annotation");
        assertThat(json).extractingJsonPathBooleanValue("$.paid").isFalse();
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\n"
                + "    \"annotation\": \"Valid annotation for testing\",\n"
                + "    \"category\": 1,\n"
                + "    \"description\": \"Valid description for testing\",\n"
                + "    \"eventDate\": \"2026-05-01 12:00:00\",\n"
                + "    \"location\": { \"lat\": 55.75, \"lon\": 37.62 },\n"
                + "    \"paid\": true,\n"
                + "    \"participantLimit\": 5,\n"
                + "    \"requestModeration\": false,\n"
                + "    \"title\": \"Valid title\"\n"
                + "}";
        var dto = jacksonTester.parse(content).getObject();
        assertThat(dto.getPaid()).isTrue();
        assertThat(dto.getParticipantLimit()).isEqualTo(5);
        assertThat(dto.getRequestModeration()).isFalse();
    }
}
