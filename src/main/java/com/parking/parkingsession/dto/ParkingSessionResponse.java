package com.parking.parkingsession.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.parking.parkingsession.entity.ParkingSessionStatus;

import lombok.Data;

@Data
public class ParkingSessionResponse {

    private Long id;
    private Long vehicleId;
    private Long parkingSpotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private ParkingSessionStatus status;
    private BigDecimal parkingFee;
}