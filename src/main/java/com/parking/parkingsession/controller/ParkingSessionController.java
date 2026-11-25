package com.parking.parkingsession.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.parking.common.dto.ApiResponse;
import com.parking.parkingsession.dto.ParkingSessionRequest;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.parkingsession.entity.ParkingSessionStatus;
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
            @Valid @RequestBody ParkingSessionRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        ParkingSessionResponse response =
                parkingSessionService.startSession(request, currentUser.getUsername());

        return ApiResponse.success(
                "PARKING_SESSION_STARTED",
                "Parking session started successfully",
                response
        );
    }
    
    /** The logged-in user's sessions, newest first. Optional ?status=ACTIVE|COMPLETED. */
    @GetMapping
    public ApiResponse<List<ParkingSessionResponse>> mySessions(
            @RequestParam(required = false) ParkingSessionStatus status,
            @AuthenticationPrincipal UserDetails currentUser) {

        List<ParkingSessionResponse> sessions =
                parkingSessionService.getMySessions(currentUser.getUsername(), status);

        return ApiResponse.success(
                "PARKING_SESSIONS_FETCHED",
                "Parking sessions fetched successfully",
                sessions
        );
    }

    @PostMapping("/{sessionId}/complete")
    public ApiResponse<ParkingSessionResponse> completeSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails currentUser) {

        ParkingSessionResponse response =
                parkingSessionService.completeSession(sessionId, currentUser.getUsername());

        return ApiResponse.success(
                "PARKING_SESSION_COMPLETED",
                "Parking session completed successfully",
                response
        );
    }
}