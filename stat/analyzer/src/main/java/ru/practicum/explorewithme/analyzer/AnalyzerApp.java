package ru.practicum.explorewithme.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

@SpringBootApplication(scanBasePackages = "ru.practicum.explorewithme")
@EnableJpaRepositories(basePackages = "ru.practicum.explorewithme")
@EntityScan(basePackages = "ru.practicum.explorewithme")
public class AnalyzerApp {

    public static void main(String[] args) {
        createDatabaseIfAbsent();
        SpringApplication.run(AnalyzerApp.class, args);
    }

    private static void createDatabaseIfAbsent() {
        String port = System.getenv().getOrDefault("POSTGRES_PORT", "5432");
        String user = System.getenv().getOrDefault("POSTGRES_USER", "postgres");
        String password = System.getenv().getOrDefault("POSTGRES_PASSWORD", "54321");
        String adminUrl = "jdbc:postgresql://localhost:" + port + "/postgres";
        try (Connection conn = DriverManager.getConnection(adminUrl, user, password)) {
            try (ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT 1 FROM pg_database WHERE datname = 'ewm_analyzer_db'")) {
                if (!rs.next()) {
                    conn.createStatement().executeUpdate("CREATE DATABASE ewm_analyzer_db");
                }
            }
        } catch (Exception ignored) {
        }
    }
}
