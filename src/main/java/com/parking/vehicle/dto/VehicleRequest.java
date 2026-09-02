package com.parking.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleRequest {
	
	@NotBlank
	private String plateNumber;
	@NotBlank
    private String type;
    private String color;
    @NotNull
    private Long userId;
    
	
    
    
}
