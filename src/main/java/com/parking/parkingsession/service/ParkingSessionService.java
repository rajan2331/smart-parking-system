package com.parking.parkingsession.service;

import java.util.List;

import com.parking.parking.entity.ParkingSpot;
import com.parking.parkingsession.dto.ParkingSessionRequest;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.parkingsession.entity.ParkingSession;
import com.parking.parkingsession.entity.ParkingSessionStatus;
import com.parking.vehicle.entity.Vehicle;

public interface ParkingSessionService {

    /** Walk-in parking. */
    ParkingSessionResponse startSession(ParkingSessionRequest request, String ownerEmail);

    /** Used by reservation check-in; ownership and the reservation itself were already checked. */
    ParkingSession startSessionForReservation(Vehicle vehicle, ParkingSpot parkingSpot);

    ParkingSessionResponse completeSession(Long sessionId, String ownerEmail);

    /** The user's sessions, newest first; status is optional (null = all). */
    List<ParkingSessionResponse> getMySessions(String ownerEmail, ParkingSessionStatus status);
}
