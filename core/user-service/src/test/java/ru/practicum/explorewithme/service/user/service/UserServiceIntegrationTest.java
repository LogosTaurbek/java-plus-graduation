package ru.practicum.explorewithme.service.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.dto.UserDto;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class UserServiceIntegrationTest {

    @SuppressWarnings("unused")
    @Autowired
    private UserService userService;

    @Test
    void shouldCreateAndRetrieveUsers() {
        UserDto created = userService.registerUser(new NewUserRequest("alice@example.com", "Алиса"));
        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("alice@example.com");

        List<UserDto> users = userService.getUsers(null, 0, 10);
        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getId()).isEqualTo(created.getId());
    }

    @Test
    void shouldFilterByIds() {
        UserDto u1 = userService.registerUser(new NewUserRequest("bob@example.com", "Боб"));
        userService.registerUser(new NewUserRequest("carol@example.com", "Кэрол"));
        UserDto u3 = userService.registerUser(new NewUserRequest("dave@example.com", "Дейв"));

        List<UserDto> filtered = userService.getUsers(List.of(u1.getId(), u3.getId()), 0, 10);
        assertThat(filtered).hasSize(2);
        assertThat(filtered).extracting(UserDto::getId).containsExactlyInAnyOrder(u1.getId(), u3.getId());
    }

    @Test
    void shouldSupportPagination() {
        for (int i = 1; i <= 5; i++) {
            userService.registerUser(new NewUserRequest("user" + i + "@example.com", "Пользователь " + i));
        }

        List<UserDto> page1 = userService.getUsers(null, 0, 2);
        List<UserDto> page2 = userService.getUsers(null, 2, 2);

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(2);
        assertThat(page1.getFirst().getId()).isLessThan(page2.getFirst().getId());
    }

    @Test
    void shouldThrowConflictOnDuplicateEmail() {
        userService.registerUser(new NewUserRequest("dup@example.com", "Первый"));
        assertThatThrownBy(() ->
                userService.registerUser(new NewUserRequest("dup@example.com", "Второй"))
        ).isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email уже существует");
    }

    @Test
    void shouldDeleteUserAndFailOnMissing() {
        UserDto created = userService.registerUser(new NewUserRequest("delete@example.com", "Удаляемый"));
        userService.deleteUser(created.getId());
        // После удаления пользователь не должен находиться
        List<UserDto> users = userService.getUsers(List.of(created.getId()), 0, 10);
        assertThat(users).isEmpty();
    }

    @Test
    void deleteUser_NotFound_ShouldThrow() {
        assertThatThrownBy(() -> userService.deleteUser(Long.MAX_VALUE))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getUsers_ShouldReturnSortedByIdAscending() {
        userService.registerUser(new NewUserRequest("sort2@example.com", "b"));
        userService.registerUser(new NewUserRequest("sort1@example.com", "a"));

        List<UserDto> users = userService.getUsers(null, 0, 10);
        // проверка сортировки по возрастанию id
        assertThat(users).isSortedAccordingTo(Comparator.comparing(UserDto::getId));
    }
}