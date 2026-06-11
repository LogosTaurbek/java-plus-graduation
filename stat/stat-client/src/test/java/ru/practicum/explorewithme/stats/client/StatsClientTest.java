package ru.practicum.explorewithme.stats.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.practicum.explorewithme.stats.dto.EndpointHitDTO;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatsClientTest {

    private MockWebServer mockWebServer;
    private StatsClient statsClient;
    private ObjectMapper objectMapper;
    private DiscoveryClient discoveryClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        discoveryClient = mock(DiscoveryClient.class);
        ServiceInstance serviceInstance = mock(ServiceInstance.class);
        when(serviceInstance.getHost()).thenReturn(mockWebServer.getHostName());
        when(serviceInstance.getPort()).thenReturn(mockWebServer.getPort());
        when(discoveryClient.getInstances(anyString())).thenReturn(List.of(serviceInstance));

        statsClient = new StatsClient("stat-server", WebClient.builder(), discoveryClient);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // поддержка LocalDateTime
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    // ----------------- saveHit -----------------
    @Test
    void saveHit_ShouldSendPostWithCorrectBody() throws Exception {
        EndpointHitDTO hit = new EndpointHitDTO(null, "app", "/uri", "1.1.1.1",
                LocalDateTime.of(2025, 5, 5, 12, 0, 0));
        String expectedBody = objectMapper.writeValueAsString(hit);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("\"Hit saved\"")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        statsClient.saveHit(hit);

        // Ждём async запроса
        RecordedRequest request = mockWebServer.takeRequest(2, java.util.concurrent.TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/hit");
        assertThat(request.getHeader(HttpHeaders.CONTENT_TYPE))
                .contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(request.getBody().readUtf8()).isEqualTo(expectedBody);
    }

    @Test
    void saveHit_WhenServerError_ShouldNotThrow() {
        EndpointHitDTO hit = new EndpointHitDTO(null, "app", "/uri", "1.1.1.1", LocalDateTime.now());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Async — не должен бросать исключение
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> statsClient.saveHit(hit));
    }

    // ----------------- getStats -----------------
    @Test
    void getStats_ShouldSendCorrectQueryParams() throws Exception {
        LocalDateTime start = LocalDateTime.of(2020, 5, 5, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2030, 5, 5, 0, 0, 0);
        List<String> uris = List.of("/events", "/events/5");
        Boolean unique = true;

        String responseBody = "[{\"app\":\"ewm\",\"uri\":\"/events\",\"hits\":10}]";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        ResponseEntity<List<ViewStatsDTO>> response = statsClient.getStats(start, end, uris, unique);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        String path = request.getPath();
        assertThat(path).startsWith("/stats?");

        // Проверяем параметры с учётом фактического кодирования
        assertThat(path).contains("start=" + FORMATTER.format(start).replace(" ", "%20"));
        assertThat(path).contains("end=" + FORMATTER.format(end).replace(" ", "%20"));
        assertThat(path).contains("uris=/events&uris=/events/5"); // без %2F
        assertThat(path).contains("unique=true");
    }

    @Test
    void getStats_WhenOptionalParamsNull_ShouldNotSendThem() throws Exception {
        LocalDateTime start = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 1, 0, 0, 0);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        statsClient.getStats(start, end, null, null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).doesNotContain("uris");
        assertThat(request.getPath()).doesNotContain("unique");
    }

    @Test
    void getStats_WhenEmptyUris_ShouldNotSendThem() throws Exception {
        LocalDateTime start = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 1, 0, 0, 0);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        statsClient.getStats(start, end, List.of(), false);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).doesNotContain("uris");
    }

    // ----------------- обработка ошибок и исключений -----------------
    @Test
    void getStats_WhenServerReturns4xx_ShouldThrowException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // StatsClient.getStats() не имеет onErrorResume, поэтому ожидаем исключение
        assertThrows(WebClientResponseException.class, () ->
                statsClient.getStats(LocalDateTime.now(), LocalDateTime.now().plusDays(1), null, null));
    }

    @Test
    void saveHit_WhenConnectionRefused_ShouldNotThrow() throws IOException {
        EndpointHitDTO hit = new EndpointHitDTO(null, "app", "/uri", "1.1.1.1", LocalDateTime.now());
        mockWebServer.shutdown();

        // Async — не должен бросать исключение даже при недоступном сервере
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> statsClient.saveHit(hit));
    }
}