package com.parking.reservation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Reservation rules, configurable under app.reservation.* */
@ConfigurationProperties(prefix = "app.reservation")
public record ReservationProperties(
        /** Shortest allowed booking. */
        @DefaultValue("30") int minDurationMinutes,
        /** Longest allowed booking. */
        @DefaultValue("24") int maxDurationHours,
        /** How far ahead the start time may be. */
        @DefaultValue("7") int maxDaysAhead,
        /** CONFIRMED (upcoming) reservations a user may hold at once. */
        @DefaultValue("3") int maxActivePerUser,
        /** Check-in opens this many minutes before the start time. */
        @DefaultValue("15") int earlyCheckInMinutes,
        /** No-show: not checked in this many minutes after the start time -> EXPIRED. */
        @DefaultValue("15") int noShowGraceMinutes,
        /** Walk-ins can't take a spot whose next reservation starts within this many minutes. */
        @DefaultValue("120") int walkInBufferMinutes,
        /** A start time this many minutes in the past is still accepted (clock skew / slow clients). */
        @DefaultValue("5") int startTimeToleranceMinutes) {
}
