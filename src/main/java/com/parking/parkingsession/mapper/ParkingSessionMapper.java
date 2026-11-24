package com.parking.parkingsession.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.parking.parkingsession.dto.ParkingSessionRequest;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.parkingsession.entity.ParkingSession;

@Mapper(componentModel = "spring")
public interface ParkingSessionMapper {

    ParkingSession toEntity(ParkingSessionRequest request);

    // The entity holds Vehicle / ParkingSpot objects; the response exposes only their ids.
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "parkingSpot.id", target = "parkingSpotId")
    ParkingSessionResponse toResponse(ParkingSession session);
}
