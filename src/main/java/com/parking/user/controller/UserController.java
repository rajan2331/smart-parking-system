package com.parking.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.parking.common.exception.UserAlreadyExistsException;
import com.parking.user.dto.UserRequest;
import com.parking.user.dto.UserResponse;
import com.parking.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	
	private  final UserService userService;
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse register(@Valid @RequestBody UserRequest request) throws UserAlreadyExistsException
	{
		UserResponse response = userService.register(request);
		return response;
	}
}
