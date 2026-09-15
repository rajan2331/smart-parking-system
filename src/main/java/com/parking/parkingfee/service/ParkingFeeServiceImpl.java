package com.parking.parkingfee.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.parking.parking.entity.ParkingSpotType;
import com.parking.parkingrate.service.ParkingRateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParkingFeeServiceImpl implements ParkingFeeService {

    private final ParkingRateService parkingRateService;

    @Override
    public BigDecimal calculateFee(ParkingSpotType spotType,LocalDateTime entryTime,LocalDateTime exitTime) {

        long minutes = Duration.between(entryTime, exitTime).toMinutes();

        long hours = (long) Math.ceil(minutes / 60.0);

        hours = Math.max(hours, 1);

        BigDecimal hourlyRate =
                parkingRateService.getHourlyRate(spotType);

        return hourlyRate.multiply(BigDecimal.valueOf(hours));
    }
}