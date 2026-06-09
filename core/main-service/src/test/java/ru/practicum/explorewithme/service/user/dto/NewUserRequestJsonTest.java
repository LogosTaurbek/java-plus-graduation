package ru.practicum.explorewithme.service.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("TextBlockMigration")
@JsonTest
class NewUserRequestJsonTest {

    @SuppressWarnings("unused")
    @Autowired
    private JacksonTester<NewUserRequest> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        NewUserRequest request = new NewUserRequest("user@example.com", "Иван Иванов");
        var json = jacksonTester.write(request);
        assertThat(json).hasJsonPathStringValue("$.email");
        assertThat(json).hasJsonPathStringValue("$.name");
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo("user@example.com");
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Иван Иванов");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\n"
                + "    \"email\": \"petrov@example.com\",\n"
                + "    \"name\": \"Петр Петров\"\n"
                + "}";
        var dto = jacksonTester.parse(content).getObject();
        assertThat(dto.getEmail()).isEqualTo("petrov@example.com");
        assertThat(dto.getName()).isEqualTo("Петр Петров");
    }

    @Test
    void testDeserialize_InvalidEmailFormat_ShouldStillParse() throws Exception {
        // JSON-тест не проверяет валидацию, только формат
        String content = "{\n"
                + "    \"email\": \"not-an-email\",\n"
                + "    \"name\": \"Имя\"\n"
                + "}";
        var dto = jacksonTester.parse(content).getObject();
        assertThat(dto.getEmail()).isEqualTo("not-an-email");
    }
}