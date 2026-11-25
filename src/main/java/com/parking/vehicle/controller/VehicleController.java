package com.parking.vehicle.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.parking.common.dto.ApiResponse;
import com.parking.common.exception.ErrorCode;
import com.parking.vehicle.dto.VehicleRequest;
import com.parking.vehicle.dto.VehicleResponse;
import com.parking.vehicle.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

	private final VehicleService vehicleService ;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<?> register(@Valid @RequestBody VehicleRequest dto,
			@AuthenticationPrincipal UserDetails currentUser)
	{
		// currentUser is set by JwtAuthenticationFilter; its username is the email from the token
		VehicleResponse response = vehicleService.register(dto, currentUser.getUsername());
		return ApiResponse.success(ErrorCode.VEHICLE_REGISTERED.getCode(),"Vehicle registered successfully",response);
	}

	/** Vehicles owned by the logged-in user, newest first. */
	@GetMapping
	public ApiResponse<List<VehicleResponse>> myVehicles(@AuthenticationPrincipal UserDetails currentUser) {
		List<VehicleResponse> vehicles = vehicleService.getMyVehicles(currentUser.getUsername());
		return ApiResponse.success("VEHICLES_FETCHED", "Vehicles fetched successfully", vehicles);
	}
}
