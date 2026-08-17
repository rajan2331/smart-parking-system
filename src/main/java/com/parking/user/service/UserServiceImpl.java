package com.parking.user.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.parking.common.exception.UserAlreadyExistsException;
import com.parking.user.dto.UserRequest;
import com.parking.user.dto.UserResponse;
import com.parking.user.entity.User;
import com.parking.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	
	private final UserRepository userRepository;
	
	@Override
	public UserResponse register(UserRequest request) throws UserAlreadyExistsException {
		System.out.println(request.getName() +"is registered and it email is "+request.getEmail());
		User userEntity = new User();
		BeanUtils.copyProperties(request, userEntity);
		if (userRepository.existsByEmail(request.getEmail())) {
		    throw new UserAlreadyExistsException("User already exists");
		}
			userRepository.save(userEntity);
		UserResponse response = new UserResponse();
		response.setId(userEntity.getId());
		response.setEmail(request.getEmail());
		response.setCreatedAt(userEntity.getCreatedAt());
		response.setMobile(request.getMobile());
		response.setName(request.getName());
		return response;
	}

}
