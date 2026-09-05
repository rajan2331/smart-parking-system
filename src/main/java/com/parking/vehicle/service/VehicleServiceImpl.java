package com.parking.vehicle.service;


import org.springframework.stereotype.Service;

import com.parking.common.exception.VehicleAlreadyRegisteredException;
import com.parking.user.entity.User;
import com.parking.user.repository.UserRepository;
import com.parking.common.exception.UserNotFoundException;
import com.parking.vehicle.dto.VehicleRequest;
import com.parking.vehicle.dto.VehicleResponse;
import com.parking.vehicle.entity.Vehicle;
import com.parking.vehicle.mapper.VehicleMapper;
import com.parking.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

	private final VehicleRepository vehicleRepository;
	private final UserRepository userRepository;
	private final VehicleMapper vehicleMapper;
	@Override
	public VehicleResponse register(VehicleRequest request) {
		
		if(vehicleRepository.existsByPlateNumber(request.getPlateNumber()))
			  throw new VehicleAlreadyRegisteredException("Vehicle already exists");
		
		 User user = userRepository.findById(request.getUserId()).orElseThrow(() ->
         new UserNotFoundException("User not found")
				 );
	    Vehicle vehicle = vehicleMapper.toEntity(request);

        vehicle.setUser(user);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return vehicleMapper.toResponse(savedVehicle);
    
	}

}
