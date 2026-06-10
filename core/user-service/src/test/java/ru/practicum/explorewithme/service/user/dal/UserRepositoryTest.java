package ru.practicum.explorewithme.service.user.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.service.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @SuppressWarnings("unused")
    @Autowired
    private TestEntityManager em;

    @SuppressWarnings("unused")
    @Autowired
    private UserRepository userRepository;

    private User user1, user2, user3;

    @BeforeEach
    void setUp() {
        user1 = createUser("one@example.com", "Пользователь Один");
        user2 = createUser("two@example.com", "Пользователь Два");
        user3 = createUser("three@example.com", "Пользователь Три");
    }

    private User createUser(String email, String name) {
        User user = new User(null, email, name);
        return em.persist(user);
    }

    @Test
    void existsByEmail_ShouldReturnTrueForExisting() {
        assertThat(userRepository.existsByEmail("one@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("unknown@example.com")).isFalse();
    }

    @Test
    void save_ShouldGenerateId() {
        User newUser = new User(null, "new@mail.com", "Новый");
        User saved = userRepository.save(newUser);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void save_DuplicateEmail_ShouldThrowException() {
        User duplicate = new User(null, user1.getEmail(), "Дубликат");
        assertThatThrownBy(() -> userRepository.save(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAllByIdIn_ShouldReturnRequestedUsers() {
        List<Long> ids = List.of(user1.getId(), user3.getId());
        Pageable pageable = PageRequest.of(0, 10);
        var page = userRepository.findAllByIdIn(ids, pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(User::getId)
                .containsExactlyInAnyOrder(user1.getId(), user3.getId());
    }

    @Test
    void findAllByIdIn_ShouldRespectPagination() {
        List<Long> allIds = List.of(user1.getId(), user2.getId(), user3.getId());
        Pageable firstPage = PageRequest.of(0, 2);
        var page = userRepository.findAllByIdIn(allIds, firstPage);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);

        Pageable secondPage = PageRequest.of(1, 2);
        page = userRepository.findAllByIdIn(allIds, secondPage);
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findAllByIdIn_EmptyIdsList_ShouldReturnEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        var page = userRepository.findAllByIdIn(List.of(), pageable);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        userRepository.deleteById(user1.getId());
        em.flush();
        assertThat(em.find(User.class, user1.getId())).isNull();
    }
}