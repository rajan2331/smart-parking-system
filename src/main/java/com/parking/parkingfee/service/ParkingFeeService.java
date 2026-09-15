package com.parking.parkingfee.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.parking.parking.entity.ParkingSpotType;

public interface ParkingFeeService {

    BigDecimal calculateFee( ParkingSpotType spotType,LocalDateTime entryTime,LocalDateTime exitTime);
}