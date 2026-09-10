package com.parking.parkingrate.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parking.parking.entity.ParkingSpotType;
import com.parking.parkingrate.entity.ParkingRate;

public interface ParkingRateRepository
        extends JpaRepository<ParkingRate, Long> {

    Optional<ParkingRate> findBySpotType(ParkingSpotType spotType);
}