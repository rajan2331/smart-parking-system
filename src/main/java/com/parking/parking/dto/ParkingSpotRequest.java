package com.parking.parking.dto;

import com.parking.parking.entity.ParkingSpotType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParkingSpotRequest {

    @NotBlank
    private String spotNumber;

    @NotNull
    private ParkingSpotType type;

}