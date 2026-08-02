package com.parking.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.user.dto.UserRequest;
import com.parking.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	
	private  final UserService service;
	@PostMapping
	public ResponseEntity<String> register(@RequestBody UserRequest user)
	{
		service.register(user);
		return ResponseEntity.status(HttpStatus.CREATED).body("User Registered Successfully");
	}
}
