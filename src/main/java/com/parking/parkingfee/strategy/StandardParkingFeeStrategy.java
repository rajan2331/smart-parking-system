package com.parking.parkingfee.strategy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.parking.parking.entity.ParkingSpotType;
import com.parking.parkingrate.service.ParkingRateService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StandardParkingFeeStrategy implements ParkingFeeStrategy {

    private final ParkingRateService parkingRateService;

    @Override
    public boolean supports(ParkingSpotType spotType) {
        return true;
    }

    @Override
    public BigDecimal calculate(
            ParkingSpotType spotType,
            LocalDateTime entryTime,
            LocalDateTime exitTime) {

        long minutes = Duration.between(entryTime, exitTime).toMinutes();

        long hours = (long) Math.ceil(minutes / 60.0);
        hours = Math.max(hours, 1);

        BigDecimal hourlyRate =
                parkingRateService.getHourlyRate(spotType);

        return hourlyRate.multiply(BigDecimal.valueOf(hours));
    }
}