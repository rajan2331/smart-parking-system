package com.parking.reservation.config;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ReservationProperties.class)
public class ReservationConfig {

    /** Injected instead of calling LocalDateTime.now() directly, so time rules are unit-testable. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
