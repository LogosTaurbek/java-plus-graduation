package ru.practicum.explorewithme.aggregator;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AggregatorService {

    private static final String SIMILARITY_TOPIC = "stats.events-similarity.v1";

    // [eventId][userId] = максимальный вес пользователя для мероприятия
    private final Map<Long, Map<Long, Double>> userWeights = new HashMap<>();
    // [eventId] = сумма всех максимальных весов пользователей
    private final Map<Long, Double> eventWeightSums = new HashMap<>();
    // [min(A,B)][max(A,B)] = S_min(A,B)
    private final Map<Long, Map<Long, Double>> minWeightSums = new HashMap<>();

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    @KafkaListener(topics = "stats.user-actions.v1")
    public void handleUserAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double newWeight = getWeight(action.getActionType());

        double oldWeight = userWeights.getOrDefault(eventId, Collections.emptyMap())
                .getOrDefault(userId, 0.0);

        if (newWeight <= oldWeight) return;

        userWeights.computeIfAbsent(eventId, k -> new HashMap<>()).put(userId, newWeight);

        double delta = newWeight - oldWeight;
        eventWeightSums.merge(eventId, delta, Double::sum);

        for (Map.Entry<Long, Map<Long, Double>> entry : userWeights.entrySet()) {
            long eventB = entry.getKey();
            if (eventB == eventId) continue;

            Double wB = entry.getValue().get(userId);
            if (wB == null) continue;

            long minId = Math.min(eventId, eventB);
            long maxId = Math.max(eventId, eventB);

            double minOld = Math.min(oldWeight, wB);
            double minNew = Math.min(newWeight, wB);
            double deltaMin = minNew - minOld;

            minWeightSums.computeIfAbsent(minId, k -> new HashMap<>())
                    .merge(maxId, deltaMin, Double::sum);

            double sMin = minWeightSums.get(minId).get(maxId);
            double sA = eventWeightSums.getOrDefault(eventId, 0.0);
            double sB = eventWeightSums.getOrDefault(eventB, 0.0);
            double score = sMin / (Math.sqrt(sA) * Math.sqrt(sB));

            EventSimilarityAvro similarity = EventSimilarityAvro.newBuilder()
                    .setEventA(minId)
                    .setEventB(maxId)
                    .setScore(score)
                    .setTimestamp(action.getTimestamp())
                    .build();

            kafkaTemplate.send(SIMILARITY_TOPIC, similarity);
        }
    }

    private double getWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
