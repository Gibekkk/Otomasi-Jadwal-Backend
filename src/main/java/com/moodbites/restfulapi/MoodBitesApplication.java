package com.moodbites.restfulapi;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MoodBitesApplication implements CommandLineRunner {

    @Value("${server.port}")
    private String runningPort;

    public static void main(String[] args) {
        SpringApplication.run(MoodBitesApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Central Indonesia Time: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        System.out.println("UTC Time: "
                + LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        System.out.println("Server Running On: http://localhost:" + runningPort);
        System.out.println("Documentation On: http://localhost:" + runningPort + "/swagger-ui");
        // initializeSeeder();
    }

    // public void initializeSeeder() {
    // }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Makassar"));
    }
}