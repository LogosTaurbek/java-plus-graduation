package ru.practicum.explorewithme.service.event.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private EventRepository eventRepository;

    private User initiator;
    private Category category;

    @BeforeEach
    void setUp() {
        initiator = new User(null, "test@example.com", "Test");
        em.persist(initiator);
        category = new Category(null, "Концерты");
        em.persist(category);
    }

    @Test
    void findAllByInitiatorId_PaginationWorks() {
        createEvent("Event 1", initiator, category);
        createEvent("Event 2", initiator, category);
        Pageable pageable = PageRequest.of(0, 1);
        var page = eventRepository.findAllByInitiatorId(initiator.getId(), pageable);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findByIdAndInitiatorId_ReturnsCorrectEvent() {
        Event event = createEvent("My Event", initiator, category);
        var found = eventRepository.findByIdAndInitiatorId(event.getId(), initiator.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("My Event");
    }

    @Test
    void findByIdAndInitiatorId_WrongUser_ReturnsEmpty() {
        Event event = createEvent("Event", initiator, category);
        var found = eventRepository.findByIdAndInitiatorId(event.getId(), 999L);
        assertThat(found).isEmpty();
    }

    private Event createEvent(String title, User user, Category cat) {
        Event e = new Event();
        e.setTitle(title);
        e.setAnnotation("annotation for test");
        e.setDescription("description for test");
        e.setEventDate(LocalDateTime.now().plusDays(1));
        e.setLocation(new ru.practicum.explorewithme.service.event.model.Location(55f, 37f));
        e.setPaid(false);
        e.setParticipantLimit(0);
        e.setRequestModeration(true);
        e.setState(EventState.PENDING);
        e.setCreatedOn(LocalDateTime.now());
        e.setInitiator(user);
        e.setCategory(cat);
        return em.persist(e);
    }
}
