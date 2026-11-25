package com.parking.parkingsession.service;

import java.util.List;

import com.parking.parkingsession.dto.ParkingSessionRequest;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.parkingsession.entity.ParkingSessionStatus;

public interface ParkingSessionService {

    ParkingSessionResponse startSession(ParkingSessionRequest request, String ownerEmail);
    ParkingSessionResponse completeSession(Long sessionId, String ownerEmail);

    /** The user's sessions, newest first; status is optional (null = all). */
    List<ParkingSessionResponse> getMySessions(String ownerEmail, ParkingSessionStatus status);
}
