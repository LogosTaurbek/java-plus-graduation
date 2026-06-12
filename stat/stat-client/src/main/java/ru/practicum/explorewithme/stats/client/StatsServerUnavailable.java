package ru.practicum.explorewithme.stats.client;

public class StatsServerUnavailable extends RuntimeException {
    public StatsServerUnavailable(String message, Throwable cause) {
        super(message, cause);
    }
}
