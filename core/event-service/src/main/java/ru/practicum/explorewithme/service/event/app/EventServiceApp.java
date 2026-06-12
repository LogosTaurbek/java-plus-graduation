package ru.practicum.explorewithme.service.event.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "ru.practicum.explorewithme")
@EnableFeignClients(basePackages = "ru.practicum.explorewithme")
@EnableJpaRepositories(basePackages = "ru.practicum.explorewithme")
@EntityScan(basePackages = "ru.practicum.explorewithme")
public class EventServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApp.class, args);
    }
}
