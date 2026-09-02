package com.parking.vehicle.service;

import com.parking.vehicle.dto.VehicleRequest;
import com.parking.vehicle.dto.VehicleResponse;

public interface VehicleService {

	public VehicleResponse register(VehicleRequest vehicle);
}
