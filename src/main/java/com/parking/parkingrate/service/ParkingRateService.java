package com.parking.parkingrate.service;


import com.parking.parking.entity.ParkingSpotType;

import java.math.BigDecimal;

public interface ParkingRateService {

    BigDecimal getHourlyRate(ParkingSpotType spotType);
}
