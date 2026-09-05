package com.parking.vehicle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.parking.vehicle.dto.VehicleRequest;
import com.parking.vehicle.dto.VehicleResponse;
import com.parking.vehicle.entity.Vehicle;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Vehicle toEntity(VehicleRequest request);

    VehicleResponse toResponse(Vehicle vehicle);
}
