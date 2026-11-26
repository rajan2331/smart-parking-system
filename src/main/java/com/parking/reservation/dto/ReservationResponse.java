package com.parking.reservation.dto;

import java.time.LocalDateTime;

import com.parking.parking.entity.ParkingSpotType;
import com.parking.reservation.entity.ReservationStatus;

import lombok.Data;

@Data
public class ReservationResponse {

    private Long id;
    private Long vehicleId;
    private String plateNumber;
    private Long parkingSpotId;
    private String spotNumber;
    private ParkingSpotType spotType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    /** Set after check-in. */
    private Long parkingSessionId;
    private LocalDateTime createdAt;
}
