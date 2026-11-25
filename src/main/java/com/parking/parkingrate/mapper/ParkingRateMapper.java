package com.parking.parkingrate.mapper;

import org.mapstruct.Mapper;

import com.parking.parkingrate.dto.ParkingRateResponse;
import com.parking.parkingrate.entity.ParkingRate;

@Mapper(componentModel = "spring")
public interface ParkingRateMapper {

    ParkingRateResponse toResponse(ParkingRate rate);
}
