package ru.practicum.explorewithme.stats.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ViewStatsDTOJsonTest {

    @SuppressWarnings("unused")
    @Autowired
    private JacksonTester<ViewStatsDTO> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        var dto = new ViewStatsDTO("ewm-main-service", "/events", 42L);
        var json = jacksonTester.write(dto);
        assertThat(json).hasJsonPathStringValue("$.app");
        assertThat(json).hasJsonPathStringValue("$.uri");
        assertThat(json).hasJsonPathNumberValue("$.hits");
        assertThat(json).extractingJsonPathNumberValue("$.hits").isEqualTo(42);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\n" +
                "    \"app\": \"app1\",\n" +
                "    \"uri\": \"/uri\",\n" +
                "    \"hits\": 7\n" +
                "}";
        var dto = jacksonTester.parse(content).getObject();
        assertThat(dto.getApp()).isEqualTo("app1");
        assertThat(dto.getUri()).isEqualTo("/uri");
        assertThat(dto.getHits()).isEqualTo(7L);
    }
}