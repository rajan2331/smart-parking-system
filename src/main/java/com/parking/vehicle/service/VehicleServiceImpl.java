package com.parking.vehicle.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.parking.common.exception.VehicleAlreadyRegisteredException;
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
	@Override
	public VehicleResponse register(VehicleRequest vehicle) {
		
		if(vehicleRepository.existsByPlateNumber(vehicle.getPlateNumber()))
			  throw new VehicleAlreadyRegisteredException("Vehicle already exists");
		
		      List<Vehicle> vehicleList = vehicleRepository.findByUserId(vehicle.getUserId());
		      for(Vehicle veh : vehicleList)
		      {
		    	  if(veh.getUser().getId()==vehicle.getUserId())
		    		  throw new UserNotFoundException("User already registered with this vehicle");
		      }
			  
		//VehicleMapper mapper = new VehicleMapper();
		
	//	 vehicleRepository.save();
		 return null;
		
	}

}
