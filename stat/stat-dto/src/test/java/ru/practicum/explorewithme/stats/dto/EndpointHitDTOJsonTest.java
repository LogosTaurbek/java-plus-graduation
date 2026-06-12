package ru.practicum.explorewithme.stats.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class EndpointHitDTOJsonTest {

    @SuppressWarnings("unused")
    @Autowired
    private JacksonTester<EndpointHitDTO> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        var dto = new EndpointHitDTO(1L, "ewm-main-service", "/events/5", "121.0.0.1",
                LocalDateTime.of(2025, 5, 5, 12, 0, 0));
        var json = jacksonTester.write(dto);
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathStringValue("$.app");
        assertThat(json).extractingJsonPathStringValue("$.timestamp")
                .isEqualTo("2025-05-05 12:00:00");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\n"
                + "    \"app\": \"ewm-main-service\",\n"
                + "    \"uri\": \"/events\",\n"
                + "    \"ip\": \"127.0.0.1\",\n"
                + "    \"timestamp\": \"2025-01-01 10:00:00\"\n"
                + "}";
        var dto = jacksonTester.parse(content).getObject();
        assertThat(dto.getApp()).isEqualTo("ewm-main-service");
        assertThat(dto.getUri()).isEqualTo("/events");
        assertThat(dto.getIp()).isEqualTo("127.0.0.1");
        assertThat(dto.getTimestamp())
                .isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0, 0));
    }

    @Test
    void testDeserialize_WithoutId() throws Exception {
        String content = "{\n"
                + "    \"app\": \"app\",\n"
                + "    \"uri\": \"/test\",\n"
                + "    \"ip\": \"10.0.0.1\",\n"
                + "    \"timestamp\": \"2024-11-20 00:00:00\"\n"
                + "}";
        var dto = jacksonTester.parse(content).getObject();
        assertThat(dto.getId()).isNull();
    }
}