package com.parking.parkingfee.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.parking.parking.entity.ParkingSpotType;
import com.parking.parkingfee.strategy.ParkingFeeStrategy;


@Service
public class ParkingFeeServiceImpl implements ParkingFeeService {

	private final List<ParkingFeeStrategy> strategies;

    public ParkingFeeServiceImpl(List<ParkingFeeStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public BigDecimal calculateFee( ParkingSpotType spotType,LocalDateTime entryTime,LocalDateTime exitTime) {

        ParkingFeeStrategy strategy = strategies.stream()
                .filter(s -> s.supports(spotType))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No parking fee strategy found"));

        return strategy.calculate(
                spotType,
                entryTime,
                exitTime);
    }
}