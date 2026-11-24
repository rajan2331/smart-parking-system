package com.parking.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

	private final String accessToken;
	private final String tokenType;
	/** Token lifetime in seconds. */
	private final long expiresIn;
}
