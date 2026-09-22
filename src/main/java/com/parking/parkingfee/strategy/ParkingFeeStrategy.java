package com.parking.parkingfee.strategy;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.parking.parking.entity.ParkingSpotType;

public interface ParkingFeeStrategy {

    BigDecimal calculate(  ParkingSpotType spotType,  LocalDateTime entryTime,  LocalDateTime exitTime);
    boolean supports(ParkingSpotType spotType);
}
