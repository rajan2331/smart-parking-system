package com.parking.user.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.parking.user.dto.UserRequest;
import com.parking.user.entity.User;
import com.parking.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	
	@Override
	public void register(UserRequest user) {
		System.out.println(user.getName() +"is registered and it email is "+user.getEmail());
		User userEntity = new User();
		BeanUtils.copyProperties(user, userEntity);
		if(!userRepository.existsByEmail(user.getEmail()))
			userRepository.save(userEntity);

	}

}
