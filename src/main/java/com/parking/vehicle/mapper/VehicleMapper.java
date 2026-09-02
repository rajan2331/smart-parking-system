package com.parking.vehicle.mapper;

import org.mapstruct.Mapper;

import com.parking.vehicle.dto.VehicleRequest;
import com.parking.vehicle.dto.VehicleResponse;
import com.parking.vehicle.entity.Vehicle;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

	 Vehicle toEntity(VehicleRequest request);

	    VehicleResponse toResponse(Vehicle user);
}

