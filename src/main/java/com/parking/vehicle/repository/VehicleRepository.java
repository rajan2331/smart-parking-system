package com.parking.vehicle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parking.vehicle.entity.Vehicle;

public interface  VehicleRepository extends JpaRepository<Vehicle, Long> {

	   boolean existsByPlateNumber(String plateNumber);

	    List<Vehicle> findByUserId(Long userId);
}
