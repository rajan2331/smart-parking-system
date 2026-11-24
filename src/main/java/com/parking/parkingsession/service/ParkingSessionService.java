package com.parking.parkingsession.service;

import com.parking.parkingsession.dto.ParkingSessionRequest;
import com.parking.parkingsession.dto.ParkingSessionResponse;

public interface ParkingSessionService {

    ParkingSessionResponse startSession(ParkingSessionRequest request, String ownerEmail);
    ParkingSessionResponse completeSession(Long sessionId, String ownerEmail);
}