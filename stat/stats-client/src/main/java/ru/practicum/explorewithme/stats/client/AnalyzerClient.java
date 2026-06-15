package ru.practicum.explorewithme.stats.client;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.dashboard.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.dashboard.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.dashboard.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.dashboard.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.dashboard.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub stub;

    public Stream<RecommendedEventProto> getRecommendations(long userId, int maxResults) {
        try {
            UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator =
                    stub.withDeadlineAfter(2, TimeUnit.SECONDS).getRecommendationsForUser(request);

            return toStream(iterator);
        } catch (StatusRuntimeException e) {
            log.error("AnalyzerClient: ошибка getRecommendations для user {}: {}", userId, e.getMessage());
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        try {
            SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator =
                    stub.withDeadlineAfter(2, TimeUnit.SECONDS).getSimilarEvents(request);

            return toStream(iterator);
        } catch (StatusRuntimeException e) {
            log.error("AnalyzerClient: ошибка getSimilarEvents для event {}: {}", eventId, e.getMessage());
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();

            Iterator<RecommendedEventProto> iterator =
                    stub.withDeadlineAfter(2, TimeUnit.SECONDS).getInteractionsCount(request);

            return toStream(iterator);
        } catch (StatusRuntimeException e) {
            log.error("AnalyzerClient: ошибка getInteractionsCount: {}", e.getMessage());
            return Stream.empty();
        }
    }

    private <T> Stream<T> toStream(Iterator<T> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }
}
