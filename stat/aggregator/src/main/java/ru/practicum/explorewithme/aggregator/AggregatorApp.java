package ru.practicum.explorewithme.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.practicum.explorewithme")
public class AggregatorApp {

    public static void main(String[] args) {
        SpringApplication.run(AggregatorApp.class, args);
    }
}
