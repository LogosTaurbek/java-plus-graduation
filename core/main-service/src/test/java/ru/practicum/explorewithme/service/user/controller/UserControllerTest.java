package ru.practicum.explorewithme.service.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.ErrorHandler;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.dto.UserDto;
import ru.practicum.explorewithme.service.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("TextBlockMigration")
@WebMvcTest(UserController.class)
@Import(ErrorHandler.class)
class UserControllerTest {

    @SuppressWarnings("unused")
    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("unused")
    @MockBean
    private UserService userService;

    private NewUserRequest validRequest;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        validRequest = new NewUserRequest("user@example.com", "Иван Иванов");
        userDto = new UserDto(1L, "user@example.com", "Иван Иванов");
    }

    // --- POST /admin/users ---
    @Test
    void createUser_Success() throws Exception {
        when(userService.registerUser(any(NewUserRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.name").value("Иван Иванов"));

        verify(userService).registerUser(any(NewUserRequest.class));
    }

    @Test
    void createUser_ValidationFail_ShouldReturn400() throws Exception {
        // Пустое имя
        String content = "{\n"
                + "    \"email\": \"user@example.com\",\n"
                + "    \"name\": \"\"\n"
                + "}";

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Некорректный запрос"))
                .andExpect(jsonPath("$.errors").isArray());

        verify(userService, never()).registerUser(any());
    }

    @Test
    void createUser_DuplicateEmail_ShouldReturn409() throws Exception {
        when(userService.registerUser(any(NewUserRequest.class)))
                .thenThrow(new ConflictException("Email уже существует: user@example.com"));

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.reason").value("Нарушение целостности данных"))
                .andExpect(jsonPath("$.message").value("Email уже существует: user@example.com"));
    }

    // --- GET /admin/users ---
    @Test
    void getUsers_WithIds_Success() throws Exception {
        when(userService.getUsers(List.of(1L, 2L), 0, 10))
                .thenReturn(List.of(userDto));

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "2")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user@example.com"));

        verify(userService).getUsers(List.of(1L, 2L), 0, 10);
    }

    @Test
    void getUsers_DefaultPagination() throws Exception {
        when(userService.getUsers(isNull(), eq(0), eq(10)))
                .thenReturn(List.of(userDto));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());

        verify(userService).getUsers(null, 0, 10);
    }

    @Test
    void getUsers_InvalidIdsParameter_ShouldReturn400() throws Exception {
        // попытка передать не число в ids
        mockMvc.perform(get("/admin/users")
                        .param("ids", "abc"))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE /admin/users/{userId} ---
    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/admin/users/{userId}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_NotFound_ShouldReturn404() throws Exception {
        doThrow(new NotFoundException("Пользователь с id=999 не найден"))
                .when(userService).deleteUser(999L);

        mockMvc.perform(delete("/admin/users/{userId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("Требуемый объект не найден"));
    }
}