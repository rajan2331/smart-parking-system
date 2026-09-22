package com.parking.user.service;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.common.exception.EmailAlreadyRegisteredException;
import com.parking.common.exception.MobileAlreadyRegisteredException;
import com.parking.user.dto.UserRequest;
import com.parking.user.dto.UserResponse;
import com.parking.user.entity.User;
import com.parking.user.mapper.UserMapper;
import com.parking.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	
	private final UserRepository userRepository;
	
	 private final UserMapper userMapper;	
	 
	 private final PasswordEncoder passwordEncoder;
	@Override
	@Transactional
	public UserResponse register(UserRequest user)  {
		User userEntity = null;
		userEntity = userMapper.toEntity(user);
		User savedUser= null;
		if (userRepository.existsByEmail(user.getEmail())) {
		    throw new EmailAlreadyRegisteredException("User already exists");
		}
		if (userRepository.existsByMobile(user.getMobile())) {
		    throw new MobileAlreadyRegisteredException("Mobile number already registered");
		}
		user.setPassword(
		        passwordEncoder.encode(user.getPassword())
		);
		try {
			savedUser = userRepository.saveAndFlush(userEntity);
		}catch(DataIntegrityViolationException ex)
		{

	        ConstraintViolationException constraintException =
	                findConstraintViolation(ex);

	        if (constraintException != null) {

	            String constraintName =
	                    constraintException.getConstraintName();

	            if ("uk_users_email".equals(constraintName)) {
	                throw new EmailAlreadyRegisteredException(
	                        "Email is already registered");
	            }

	            if ("uk_users_mobile".equals(constraintName)) {
	                throw new MobileAlreadyRegisteredException(
	                        "Mobile number is already registered");
	            }
	        }

	        throw ex;
	    }
		
		return userMapper.toResponse(savedUser);
	}

	private ConstraintViolationException findConstraintViolation(
	        Throwable throwable) {

	    while (throwable != null) {

	        if (throwable instanceof ConstraintViolationException) {
	            return (ConstraintViolationException) throwable;
	        }

	        throwable = throwable.getCause();
	    }

	    return null;
	}

}
