package com.parking.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleRequest {
	
	@NotBlank
	private String plateNumber;
	@NotBlank
    private String type;
    private String color;
    // Owner is taken from the JWT (logged-in user), never from the request body.
    
	
    
    
}
