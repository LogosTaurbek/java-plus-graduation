package ru.practicum.explorewithme.service.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.user.dal.UserRepository;
import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.dto.UserDto;
import ru.practicum.explorewithme.service.user.mapper.UserMapper;
import ru.practicum.explorewithme.service.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto registerUser(NewUserRequest newUserRequest) {
        log.info("Регистрация нового пользователя: email={}", newUserRequest.getEmail());
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new ConflictException("Email уже существует: " + newUserRequest.getEmail());
        }
        User user = UserMapper.toEntity(newUserRequest);
        user = userRepository.save(user);
        log.debug("Пользователь сохранен с id={}", user.getId());
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        log.info("Получение пользователей: ids={}, from={}, size={}", ids, from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        if (ids != null && !ids.isEmpty()) {
            return userRepository.findAllByIdIn(ids, pageable)
                    .stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAll(pageable)
                    .stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с id={}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ИД=" + userId + " не найден");
        }
        userRepository.deleteById(userId);
        log.debug("Пользователь удален");
    }
}