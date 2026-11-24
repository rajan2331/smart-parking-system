package com.parking.auth.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.auth.dto.LoginRequest;
import com.parking.auth.dto.LoginResponse;
import com.parking.common.dto.ApiResponse;
import com.parking.common.exception.ErrorCode;
import com.parking.security.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		// Throws BadCredentialsException on wrong email/password -> handled in GlobalExceptionHandler
		Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		String token = jwtService.generateToken(auth.getName());
		LoginResponse response = new LoginResponse(token, "Bearer", jwtService.getExpirationMs() / 1000);
		return ApiResponse.success(ErrorCode.LOGIN_SUCCESS.getCode(), "Login successful", response);
	}
}
