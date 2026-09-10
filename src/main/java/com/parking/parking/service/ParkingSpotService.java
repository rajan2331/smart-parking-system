package com.parking.parking.service;

import java.util.List;

import com.parking.parking.dto.ParkingSpotRequest;
import com.parking.parking.dto.ParkingSpotResponse;

public interface ParkingSpotService {

    ParkingSpotResponse create(ParkingSpotRequest request);
    List<ParkingSpotResponse> getAvailableSpots();
}
