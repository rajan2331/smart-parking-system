package com.parking.reservation.service;

import java.time.LocalDateTime;
import java.util.List;

import com.parking.parking.dto.ParkingSpotResponse;
import com.parking.parking.entity.ParkingSpotType;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.reservation.dto.ReservationRequest;
import com.parking.reservation.dto.ReservationResponse;
import com.parking.reservation.entity.ReservationStatus;

public interface ReservationService {

    ReservationResponse create(ReservationRequest request, String ownerEmail);

    /** The user's reservations, newest start first; status is optional (null = all). */
    List<ReservationResponse> getMine(String ownerEmail, ReservationStatus status);

    ReservationResponse cancel(Long reservationId, String ownerEmail);

    /** Starts a parking session on the reserved spot; returns that session. */
    ParkingSessionResponse checkIn(Long reservationId, String ownerEmail);

    /** Spots that can be reserved for [start, end), optionally of one type. */
    List<ParkingSpotResponse> findAvailableSpots(LocalDateTime start, LocalDateTime end, ParkingSpotType type);

    /** Marks CONFIRMED reservations past the no-show grace period as EXPIRED; returns how many. */
    int expireNoShows();
}
