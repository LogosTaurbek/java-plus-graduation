package ru.practicum.explorewithme.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.practicum.explorewithme")
public class MainServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(MainServiceApp.class, args);

    }
}
