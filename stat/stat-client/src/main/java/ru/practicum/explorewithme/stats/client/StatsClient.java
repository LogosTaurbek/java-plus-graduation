package ru.practicum.explorewithme.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.explorewithme.stats.dto.EndpointHitDTO;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class StatsClient {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WebClient webClient;
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId;

    public StatsClient(@Value("${stats-service.id:stat-server}") String statsServiceId,
                       WebClient.Builder builder,
                       DiscoveryClient discoveryClient) {
        this.statsServiceId = statsServiceId;
        this.discoveryClient = discoveryClient;
        this.webClient = builder.build();

        this.retryTemplate = new RetryTemplate();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(500L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(2);
        retryTemplate.setRetryPolicy(retryPolicy);
    }

    private ServiceInstance getInstance() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(statsServiceId);
            if (instances.isEmpty()) {
                throw new RuntimeException("No instances found for " + statsServiceId);
            }
            return instances.get(0);
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId,
                    exception
            );
        }
    }

    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    public void saveHit(EndpointHitDTO hitDto) {
        try {
            URI uri = makeUri("/hit");
            webClient.post()
                    .uri(uri)
                    .bodyValue(hitDto)
                    .retrieve()
                    .toEntity(Object.class)
                    .subscribe(
                            response -> log.debug("Hit saved: {}", hitDto.getUri()),
                            error -> log.warn("Не удалось сохранить хит: {}", error.getMessage())
                    );
        } catch (Exception e) {
            log.warn("Не удалось отправить хит в статистику. Ошибка: {}", e.getMessage());
        }
    }

    public ResponseEntity<List<ViewStatsDTO>> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        URI uri = makeUri("/stats");
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.scheme(uri.getScheme())
                            .host(uri.getHost())
                            .port(uri.getPort())
                            .path(uri.getPath())
                            .queryParam("start", start.format(FORMATTER))
                            .queryParam("end", end.format(FORMATTER));
                    if (uris != null && !uris.isEmpty()) {
                        uriBuilder.queryParam("uris", uris);
                    }
                    if (unique != null) {
                        uriBuilder.queryParam("unique", unique);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .toEntityList(ViewStatsDTO.class)
                .block(Duration.ofSeconds(5));
    }
}
