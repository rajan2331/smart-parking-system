package com.parking.parkingrate.service;


import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.parking.common.exception.ParkingRateNotFoundException;
import com.parking.parking.entity.ParkingSpotType;
import com.parking.parkingrate.entity.ParkingRate;
import com.parking.parkingrate.repository.ParkingRateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParkingRateServiceImpl implements ParkingRateService {

    private final ParkingRateRepository parkingRateRepository;

    @Override
    public BigDecimal getHourlyRate(ParkingSpotType spotType) {

        ParkingRate rate = parkingRateRepository
                .findBySpotType(spotType)
                .orElseThrow(() ->
                        new ParkingRateNotFoundException(
                                "Parking rate not configured for " + spotType));

        return rate.getHourlyRate();
    }
}
