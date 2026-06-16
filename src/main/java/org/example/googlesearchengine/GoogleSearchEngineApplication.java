package org.example.googlesearchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GoogleSearchEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoogleSearchEngineApplication.class, args);
    }
}
