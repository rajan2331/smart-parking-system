package com.parking.parkingsession.mapper;

import org.mapstruct.Mapper;

import com.parking.parkingsession.dto.ParkingSessionRequest;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.parkingsession.entity.ParkingSession;

@Mapper(componentModel = "spring")
public interface ParkingSessionMapper {

    ParkingSession toEntity(ParkingSessionRequest request);

    ParkingSessionResponse toResponse(ParkingSession session);
}