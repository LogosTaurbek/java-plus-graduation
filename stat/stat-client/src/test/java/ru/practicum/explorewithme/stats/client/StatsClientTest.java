package ru.practicum.explorewithme.stats.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class StatsClientTest {

    private MockWebServer mockWebServer;
    private StatsClient statsClient;
    private ObjectMapper objectMapper;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        statsClient = new StatsClient(baseUrl, WebClient.builder());
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // поддержка LocalDateTime
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    // ----------------- saveHit -----------------
    @Test
    void saveHit_ShouldSendPostWithCorrectBodyAndReturnResponse() throws Exception {
        EndpointHitDTO hit = new EndpointHitDTO(null, "app", "/uri", "1.1.1.1",
                LocalDateTime.of(2025, 5, 5, 12, 0, 0));
        String expectedBody = objectMapper.writeValueAsString(hit);

        // Важно вернуть валидный JSON, иначе WebClient не сможет десериализовать
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("\"Hit saved\"")   // JSON-строка
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        ResponseEntity<Object> response = statsClient.saveHit(hit);

        // Проверяем HTTP-статус
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // Тело должно быть десериализовано как строка "Hit saved"
        assertThat(response.getBody()).isEqualTo("Hit saved");

        // Проверяем детали запроса
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/hit");
        assertThat(request.getHeader(HttpHeaders.CONTENT_TYPE))
                .contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(request.getBody().readUtf8()).isEqualTo(expectedBody);
    }

    @Test
    void saveHit_WhenServerError_ShouldThrowException() {
        EndpointHitDTO hit = new EndpointHitDTO(null, "app", "/uri", "1.1.1.1", LocalDateTime.now());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        ResponseEntity<Object> response = statsClient.saveHit(hit);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
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
    void saveHit_WhenConnectionRefused_ShouldThrowException() throws IOException {
        EndpointHitDTO hit = new EndpointHitDTO(null, "app", "/uri", "1.1.1.1", LocalDateTime.now());
            mockWebServer.shutdown();

        ResponseEntity<Object> response = statsClient.saveHit(hit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}