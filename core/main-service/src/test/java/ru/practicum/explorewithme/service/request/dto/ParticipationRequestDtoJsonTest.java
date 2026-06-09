// ParticipationRequestDtoJsonTest.java
package ru.practicum.explorewithme.service.request.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;

import java.time.LocalDateTime;

@JsonTest
class ParticipationRequestDtoJsonTest {

    @SuppressWarnings("unused")
    @Autowired
    private JacksonTester<ParticipationRequestDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        var dto = ParticipationRequestDto.builder()
                .id(1L)
                .requester(2L)
                .event(3L)
                .status(ParticipationRequestStatus.PENDING)
                .created(LocalDateTime.of(2025, 1, 1, 10, 0))
                .build();

        var json = jacksonTester.write(dto);
        Assertions.assertThat(json).hasJsonPathStringValue("$.status");
        Assertions.assertThat(json).extractingJsonPathStringValue("$.created")
                .isEqualTo("2025-01-01 10:00:00");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\n"
                + "    \"id\": 1,\n"
                + "    \"requester\": 2,\n"
                + "    \"event\": 3,\n"
                + "    \"status\": \"PENDING\",\n"
                + "    \"created\": \"2025-01-01 10:00:00\"\n"
                + "}";
        var dto = jacksonTester.parse(content).getObject();
        Assertions.assertThat(dto.getStatus()).isEqualTo(ParticipationRequestStatus.PENDING);
        Assertions.assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
    }
}
