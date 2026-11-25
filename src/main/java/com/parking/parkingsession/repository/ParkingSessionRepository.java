package com.parking.parkingsession.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parking.parkingsession.entity.ParkingSession;
import com.parking.parkingsession.entity.ParkingSessionStatus;

public interface ParkingSessionRepository   extends JpaRepository<ParkingSession, Long> {

	boolean existsByVehicleIdAndStatus(Long vehicleId,ParkingSessionStatus status);

	// Sessions of all vehicles owned by the user with this email, newest first
	List<ParkingSession> findByVehicleUserEmailOrderByEntryTimeDesc(String email);

	List<ParkingSession> findByVehicleUserEmailAndStatusOrderByEntryTimeDesc(
			String email, ParkingSessionStatus status);
}
