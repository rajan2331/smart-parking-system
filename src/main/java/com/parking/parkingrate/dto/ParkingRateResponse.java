package com.parking.parkingrate.dto;

import java.math.BigDecimal;

import com.parking.parking.entity.ParkingSpotType;

import lombok.Data;

@Data
public class ParkingRateResponse {

    private ParkingSpotType spotType;
    private BigDecimal hourlyRate;
}
