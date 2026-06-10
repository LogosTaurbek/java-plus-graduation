package ru.practicum.explorewithme.service.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("TextBlockMigration")
@JsonTest
class UserDtoJsonTest {

    @SuppressWarnings("unused")
    @Autowired
    private JacksonTester<UserDto> jacksonTester;

    @Test
    void testSerialize_IdShouldBePresent() throws Exception {
        UserDto dto = new UserDto(1L, "mail@mail.ru", "Сергей");
        var json = jacksonTester.write(dto);
        // В сериализованном ответе id должно присутствовать.
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).hasJsonPathStringValue("$.email");
        assertThat(json).hasJsonPathStringValue("$.name");
    }

    @Test
    void testDeserialize_IdShouldBeIgnored() throws Exception {
        String content = "{\n"
                + "    \"id\": 123,\n"
                + "    \"email\": \"ignored@id.ru\",\n"
                + "    \"name\": \"Тест\"\n"
                + "}";
        var dto = jacksonTester.parse(content).getObject();
        // id не должен быть установлен, даже если присутствует в JSON
        assertThat(dto.getId()).isNull();
        assertThat(dto.getEmail()).isEqualTo("ignored@id.ru");
        assertThat(dto.getName()).isEqualTo("Тест");
    }
}
