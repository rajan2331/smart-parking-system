package com.parking.vehicle.service;

import java.util.List;

import com.parking.vehicle.dto.VehicleRequest;
import com.parking.vehicle.dto.VehicleResponse;

public interface VehicleService {

	public VehicleResponse register(VehicleRequest vehicle, String ownerEmail);

	List<VehicleResponse> getMyVehicles(String ownerEmail);
}
