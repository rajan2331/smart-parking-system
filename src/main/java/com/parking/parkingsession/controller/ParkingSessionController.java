package com.parking.parkingsession.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.parking.common.dto.ApiResponse;
import com.parking.parkingsession.dto.ParkingSessionRequest;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.parkingsession.service.ParkingSessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/parking-sessions")
@RequiredArgsConstructor
public class ParkingSessionController {

    private final ParkingSessionService parkingSessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ParkingSessionResponse> startSession(
            @Valid @RequestBody ParkingSessionRequest request) {

        ParkingSessionResponse response =
                parkingSessionService.startSession(request);

        return ApiResponse.success(
                "PARKING_SESSION_STARTED",
                "Parking session started successfully",
                response
        );
    }
    
    @PostMapping("/{sessionId}/complete")
    public ApiResponse<ParkingSessionResponse> completeSession(
            @PathVariable Long sessionId) {

        ParkingSessionResponse response =
                parkingSessionService.completeSession(sessionId);

        return ApiResponse.success(
                "PARKING_SESSION_COMPLETED",
                "Parking session completed successfully",
                response
        );
    }
}