package com.parking.parkingrate.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.common.dto.ApiResponse;
import com.parking.parking.entity.ParkingSpotType;
import com.parking.parkingrate.dto.ParkingRateRequest;
import com.parking.parkingrate.dto.ParkingRateResponse;
import com.parking.parkingrate.service.ParkingRateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/parking-rates")
@RequiredArgsConstructor
public class ParkingRateController {

    private final ParkingRateService parkingRateService;

    /** Current hourly rates — any logged-in user. */
    @GetMapping
    public ApiResponse<List<ParkingRateResponse>> getAll() {
        return ApiResponse.success(
                "PARKING_RATES_FETCHED",
                "Parking rates fetched successfully",
                parkingRateService.getAllRates());
    }

    /** Admin only (see SecurityConfig). */
    @PutMapping("/{spotType}")
    public ApiResponse<ParkingRateResponse> update(
            @PathVariable ParkingSpotType spotType,
            @Valid @RequestBody ParkingRateRequest request) {

        return ApiResponse.success(
                "PARKING_RATE_UPDATED",
                "Parking rate updated successfully",
                parkingRateService.updateRate(spotType, request.getHourlyRate()));
    }
}
