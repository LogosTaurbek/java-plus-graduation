package ru.practicum.explorewithme.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.StreamSupport;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConfigDiagnosticLogger {

    private final ConfigurableEnvironment env;

    @PostConstruct
    public void logConfig() {
        log.info("DIAGNOSTIC: --- START DATASOURCE CONFIG ---");
        log.info("DIAGNOSTIC: spring.datasource.url: {}", env.getProperty("spring.datasource.url"));
        log.info("DIAGNOSTIC: spring.datasource.username: {}", env.getProperty("spring.datasource.username"));
        String pass = env.getProperty("spring.datasource.password");
        log.info("DIAGNOSTIC: spring.datasource.password length: {}", pass != null ? pass.length() : "NULL");
        
        log.info("DIAGNOSTIC: --- PROPERTY SOURCES ---");
        for (PropertySource<?> source : env.getPropertySources()) {
             log.info("DIAGNOSTIC: Source: {}", source.getName());
        }
        log.info("DIAGNOSTIC: --- END DATASOURCE CONFIG ---");
    }
}
