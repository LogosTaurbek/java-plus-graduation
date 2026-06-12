package ru.practicum.explorewithme.service.request.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.request.model.ParticipationRequest;
import ru.practicum.explorewithme.service.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventRequestRepositoryTest {

    @SuppressWarnings("unused")
    @Autowired
    private TestEntityManager em;

    @SuppressWarnings("unused")
    @Autowired
    private EventRequestRepository requestRepository;

    private User initiator;
    private User requester;
    private Event event;

    @BeforeEach
    void setUp() {
        initiator = new User(null, "initiator@example.com", "Initiator");
        em.persist(initiator);
        requester = new User(null, "requester@example.com", "Requester");
        em.persist(requester);

        Category category = new Category(null, "Тест");
        em.persist(category);

        event = new Event();
        event.setTitle("Event");
        event.setAnnotation("ann");
        event.setDescription("desc");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setLocation(new ru.practicum.explorewithme.service.event.model.Location(55f, 37f));
        event.setPaid(false);
        event.setParticipantLimit(5);
        event.setRequestModeration(true);
        event.setState(EventState.PUBLISHED);
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(initiator);
        event.setCategory(category);
        em.persist(event);
    }

    @Test
    void countByEventIdAndStatus_ReturnsCorrectCount() {
        User req1 = createTestUser("req1@example.com", "Req1");
        User req2 = createTestUser("req2@example.com", "Req2");
        User req3 = createTestUser("req3@example.com", "Req3");

        createRequest(req1, ParticipationRequestStatus.CONFIRMED);
        createRequest(req2, ParticipationRequestStatus.CONFIRMED);
        createRequest(req3, ParticipationRequestStatus.PENDING);

        int count = requestRepository.countByEventIdAndStatus(event.getId(), ParticipationRequestStatus.CONFIRMED);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void findAllByEventIdAndEventInitiatorId_ReturnsRequests() {
        createRequest(requester, ParticipationRequestStatus.PENDING);
        List<ParticipationRequest> result = requestRepository
                .findAllByEventIdAndEventInitiatorId(event.getId(), initiator.getId());
        assertThat(result).hasSize(1);
    }

    private void createRequest(User requester, ParticipationRequestStatus status) {
        ParticipationRequest req = new ParticipationRequest();
        req.setCreated(LocalDateTime.now());
        req.setEvent(event);
        req.setRequester(requester);
        req.setStatus(status);
        em.persist(req);
    }

    private User createTestUser(String email, String name) {
        User user = new User(null, email, name);
        return em.persist(user);
    }
}
