package ru.practicum.explorewithme.service.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("TextBlockMigration")
@JsonTest
class UserShortDtoJsonTest {

    @SuppressWarnings("unused")
    @Autowired
    private JacksonTester<UserShortDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        UserShortDto dto = new UserShortDto(5L, "Анна");
        var json = jacksonTester.write(dto);
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathStringValue("$.name");
        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(5);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\n"
                + "    \"id\": 7,\n"
                + "    \"name\": \"Кирилл\"\n"
                + "}";
        var dto = jacksonTester.parse(content).getObject();
        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getName()).isEqualTo("Кирилл");
    }
}
