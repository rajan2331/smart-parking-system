package com.parking.parkingsession.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.parking.parkingsession.entity.ParkingSession;
import com.parking.parkingsession.entity.ParkingSessionStatus;

public interface ParkingSessionRepository   extends JpaRepository<ParkingSession, Long> {
	
	boolean existsByVehicleIdAndStatus(Long vehicleId,ParkingSessionStatus status);
}