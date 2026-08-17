package com.parking.user.service;

import com.parking.common.exception.UserAlreadyExistsException;
import com.parking.user.dto.UserRequest;
import com.parking.user.dto.UserResponse;

public interface UserService {

	  UserResponse register(UserRequest userRequest) throws UserAlreadyExistsException;
}
