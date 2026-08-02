package com.parking.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {
	
	private String name;
	@NotNull
	private String mobile;
	private String email;
	
	

}
