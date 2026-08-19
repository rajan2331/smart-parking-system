package com.parking.user.service;

import com.parking.user.dto.UserRequest;
import com.parking.user.dto.UserResponse;

public interface UserService {

	  UserResponse register(UserRequest userRequest) ;
}
