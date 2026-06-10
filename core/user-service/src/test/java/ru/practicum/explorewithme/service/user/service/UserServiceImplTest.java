package ru.practicum.explorewithme.service.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.user.dal.UserRepository;
import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.dto.UserDto;
import ru.practicum.explorewithme.service.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private NewUserRequest newUserRequest;
    private User user;

    @BeforeEach
    void setUp() {
        newUserRequest = new NewUserRequest("user@example.com", "Иван Иванов");
        user = new User(1L, "user@example.com", "Иван Иванов");
    }

    // --- registerUser ---
    @Test
    void registerUser_ShouldSaveAndReturnDto() {
        when(userRepository.existsByEmail(newUserRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.registerUser(newUserRequest);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getName()).isEqualTo("Иван Иванов");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void registerUser_DuplicateEmail_ShouldThrowConflictException() {
        when(userRepository.existsByEmail(newUserRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(newUserRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email уже существует");

        verify(userRepository, never()).save(any());
    }

    // --- getUsers ---
    @Test
    void getUsers_WithIds_ShouldCallFindAllByIdIn() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(userRepository.findAllByIdIn(eq(List.of(1L, 2L)), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user)));

        List<UserDto> result = userService.getUsers(List.of(1L, 2L), 0, 10);

        assertThat(result).hasSize(1);
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getUsers_WithoutIds_ShouldCallFindAll() {
        Pageable pageable = PageRequest.of(1, 5, Sort.by("id").ascending());
        when(userRepository.findAll(eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user)));

        List<UserDto> result = userService.getUsers(null, 5, 5);

        assertThat(result).hasSize(1);
        verify(userRepository, never()).findAllByIdIn(any(), any());
    }

    @Test
    void getUsers_EmptyIdsList_ShouldCallFindAll() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(userRepository.findAll(eq(pageable))).thenReturn(new PageImpl<>(List.of()));

        List<UserDto> result = userService.getUsers(List.of(), 0, 10);

        assertThat(result).isEmpty();
        verify(userRepository, never()).findAllByIdIn(any(), any());
    }

    @Test
    void getUsers_ShouldCalculatePageCorrectly() {
        // from=7, size=3 -> page = 7/3 = 2 (остаток отбрасывается) и size=3
        Pageable expectedPageable = PageRequest.of(2, 3, Sort.by("id").ascending());
        when(userRepository.findAll(eq(expectedPageable))).thenReturn(new PageImpl<>(List.of()));

        userService.getUsers(null, 7, 3);

        verify(userRepository).findAll(expectedPageable);
    }

    // --- deleteUser ---
    @Test
    void deleteUser_ShouldDeleteWhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound_ShouldThrowNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository, never()).deleteById(any());
    }
}