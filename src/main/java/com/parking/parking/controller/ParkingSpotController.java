package com.parking.parking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.parking.common.dto.ApiResponse;
import com.parking.common.exception.ErrorCode;
import com.parking.parking.dto.ParkingSpotRequest;
import com.parking.parking.dto.ParkingSpotResponse;
import com.parking.parking.service.ParkingSpotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/parking-spots")
@RequiredArgsConstructor
public class ParkingSpotController {

    private final ParkingSpotService parkingSpotService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ParkingSpotResponse> create(
            @Valid @RequestBody ParkingSpotRequest request) {

        ParkingSpotResponse response = parkingSpotService.create(request);

        return ApiResponse.success(
                ErrorCode.PARKING_SPOT_REGISTERED.getCode(),
                "Parking spot registered successfully",
                response
        );
    }
    
    @GetMapping("/available")
    public ApiResponse<List<ParkingSpotResponse>> getAvailableSpots() {

        List<ParkingSpotResponse> spots =
                parkingSpotService.getAvailableSpots();

        return ApiResponse.success(
                "PARKING_SPOTS_FETCHED",
                "Available parking spots fetched successfully",
                spots
        );
    }
}