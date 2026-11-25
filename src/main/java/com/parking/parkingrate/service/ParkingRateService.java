package com.parking.parkingrate.service;


import java.math.BigDecimal;
import java.util.List;

import com.parking.parking.entity.ParkingSpotType;
import com.parking.parkingrate.dto.ParkingRateResponse;

public interface ParkingRateService {

    BigDecimal getHourlyRate(ParkingSpotType spotType);

    List<ParkingRateResponse> getAllRates();

    /** Sets the hourly rate for a spot type, creating the rate if it doesn't exist yet. */
    ParkingRateResponse updateRate(ParkingSpotType spotType, BigDecimal hourlyRate);
}
