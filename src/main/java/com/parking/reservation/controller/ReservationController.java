package com.parking.reservation.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.parking.parking.dto.ParkingSpotResponse;
import com.parking.parking.entity.ParkingSpotType;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.reservation.dto.ReservationRequest;
import com.parking.reservation.dto.ReservationResponse;
import com.parking.reservation.entity.ReservationStatus;
import com.parking.reservation.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /** Spots that can be booked for a window, e.g. ?start=2026-11-26T14:00:00&end=2026-11-26T16:00:00&type=EV */
    @GetMapping("/available-spots")
    public ApiResponse<List<ParkingSpotResponse>> availableSpots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) ParkingSpotType type) {

        return ApiResponse.success("AVAILABLE_SPOTS_FETCHED", "Available spots fetched successfully",
                reservationService.findAvailableSpots(start, end, type));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> create(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ApiResponse.success("RESERVATION_CREATED", "Reservation created successfully",
                reservationService.create(request, currentUser.getUsername()));
    }

    /** The logged-in user's reservations. Optional ?status=CONFIRMED|CHECKED_IN|CANCELLED|EXPIRED */
    @GetMapping
    public ApiResponse<List<ReservationResponse>> mine(
            @RequestParam(required = false) ReservationStatus status,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ApiResponse.success("RESERVATIONS_FETCHED", "Reservations fetched successfully",
                reservationService.getMine(currentUser.getUsername(), status));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ReservationResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ApiResponse.success("RESERVATION_CANCELLED", "Reservation cancelled successfully",
                reservationService.cancel(id, currentUser.getUsername()));
    }

    /** Arrive at the reserved spot: starts a parking session. */
    @PostMapping("/{id}/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ParkingSessionResponse> checkIn(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ApiResponse.success("PARKING_SESSION_STARTED", "Checked in: parking session started",
                reservationService.checkIn(id, currentUser.getUsername()));
    }
}
