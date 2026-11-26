package com.parking.reservation.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationRequest {

    @NotNull
    private Long vehicleId;

    @NotNull
    private Long parkingSpotId;

    /** ISO local date-time in server time, e.g. 2026-11-26T14:00:00 */
    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;
}
