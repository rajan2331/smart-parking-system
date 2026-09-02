package com.parking.vehicle.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class VehicleResponse {

	private Long id;
	private LocalDateTime createdAt;
	private String plateNumber;
	
}
