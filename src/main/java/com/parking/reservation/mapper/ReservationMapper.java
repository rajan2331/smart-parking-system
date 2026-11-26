package com.parking.reservation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.parking.reservation.dto.ReservationResponse;
import com.parking.reservation.entity.Reservation;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "vehicle.plateNumber", target = "plateNumber")
    @Mapping(source = "parkingSpot.id", target = "parkingSpotId")
    @Mapping(source = "parkingSpot.spotNumber", target = "spotNumber")
    @Mapping(source = "parkingSpot.type", target = "spotType")
    @Mapping(source = "parkingSession.id", target = "parkingSessionId")
    ReservationResponse toResponse(Reservation reservation);
}
