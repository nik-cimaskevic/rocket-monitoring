package com.rocket.api.common.beans.clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneOffset;

@Configuration
public class ClockConfig {

    /*
     * Provides a UTC Clock bean for consistent time handling across the application.
     * Injecting Clock instead of calling Instant.now() directly enables deterministic testing
     * by allowing a fixed clock to be substituted in tests.
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }
}