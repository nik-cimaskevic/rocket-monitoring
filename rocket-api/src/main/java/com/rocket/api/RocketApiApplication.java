package com.rocket.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class RocketApiApplication {

    private static final Logger log = LoggerFactory.getLogger(RocketApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RocketApiApplication.class, args);
    }

    @GetMapping("/log")
    public String produceLogs(@RequestParam(defaultValue = "5") int count) {
        for (int i = 1; i <= count; i++) {
            log.info("Info log message #{}", i);
            log.warn("Warning log message #{}", i);
            log.error("Error log message #{}", i);
        }
        return "Produced " + (count * 3) + " log entries";
    }
}
