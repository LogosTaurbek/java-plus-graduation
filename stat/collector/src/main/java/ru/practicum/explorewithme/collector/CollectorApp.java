package ru.practicum.explorewithme.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.practicum.explorewithme")
public class CollectorApp {

    public static void main(String[] args) {
        SpringApplication.run(CollectorApp.class, args);
    }
}
