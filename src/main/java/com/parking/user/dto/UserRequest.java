package com.parking.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {
	
	@NotBlank
	private String name;
	@NotBlank
	private String mobile;
	@NotBlank
	@Email
	private String email;
	
	

}
