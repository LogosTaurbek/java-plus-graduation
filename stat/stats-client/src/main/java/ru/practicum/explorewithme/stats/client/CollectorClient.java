package ru.practicum.explorewithme.stats.client;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.collector.ActionTypeProto;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.collector.UserActionProto;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub stub;

    public void collectUserAction(long userId, long eventId, ActionTypeProto actionType, Instant timestamp) {
        try {
            Timestamp protoTimestamp = Timestamp.newBuilder()
                    .setSeconds(timestamp.getEpochSecond())
                    .setNanos(timestamp.getNano())
                    .build();

            UserActionProto request = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(actionType)
                    .setTimestamp(protoTimestamp)
                    .build();

            stub.withDeadlineAfter(10, TimeUnit.SECONDS).collectUserAction(request);

            log.debug("CollectorClient: действие {} пользователя {} для события {} отправлено",
                    actionType, userId, eventId);
        } catch (StatusRuntimeException e) {
            log.error("CollectorClient: ошибка gRPC при отправке действия {} для события {}: {}",
                    actionType, eventId, e.getMessage());
        }
    }
}
