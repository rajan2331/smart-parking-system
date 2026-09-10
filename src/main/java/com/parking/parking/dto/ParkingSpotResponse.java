package com.parking.parking.dto;

import java.time.LocalDateTime;

import com.parking.parking.entity.ParkingSpotStatus;
import com.parking.parking.entity.ParkingSpotType;

import lombok.Data;

@Data
public class ParkingSpotResponse {

    private Long id;
    private String spotNumber;
    private ParkingSpotType type;
    private ParkingSpotStatus status;
    private LocalDateTime createdAt;
}
