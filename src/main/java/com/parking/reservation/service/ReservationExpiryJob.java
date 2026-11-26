package com.parking.reservation.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Frees spots held by no-shows: runs every minute (configurable via app.reservation.expiry-interval-ms). */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiryJob {

    private final ReservationService reservationService;

    @Scheduled(fixedDelayString = "${app.reservation.expiry-interval-ms:60000}",
               initialDelayString = "${app.reservation.expiry-interval-ms:60000}")
    public void expireNoShows() {
        int expired = reservationService.expireNoShows();
        if (expired > 0) {
            log.info("Expired {} no-show reservation(s)", expired);
        }
    }
}
