package com.parking.parking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parking.parking.entity.ParkingSpot;
import com.parking.parking.entity.ParkingSpotStatus;

public interface ParkingSpotRepository
        extends JpaRepository<ParkingSpot, Long> {

    boolean existsBySpotNumber(String spotNumber);
    List<ParkingSpot> findByStatus(ParkingSpotStatus status);
}