package com.parking.user.service;

import org.springframework.stereotype.Service;

import com.parking.user.dto.UserRequest;

@Service("userService")
public class UserServiceImpl implements UserService {

	@Override
	public void register(UserRequest user) {
		System.out.println(user.getName() +"is registered and it email is "+user.getEmail());

	}

}
