package com.parking.parking.mapper;

import org.mapstruct.Mapper;

import com.parking.parking.dto.ParkingSpotRequest;
import com.parking.parking.dto.ParkingSpotResponse;
import com.parking.parking.entity.ParkingSpot;

@Mapper(componentModel = "spring")
public interface ParkingSpotMapper {

    ParkingSpot toEntity(ParkingSpotRequest request);

    ParkingSpotResponse toResponse(ParkingSpot parkingSpot);
}