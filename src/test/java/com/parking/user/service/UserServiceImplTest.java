package com.parking.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.parking.common.exception.EmailAlreadyRegisteredException;
import com.parking.common.exception.MobileAlreadyRegisteredException;
import com.parking.common.exception.UserNotFoundException;
import com.parking.user.dto.UserRequest;
import com.parking.user.dto.UserResponse;
import com.parking.user.entity.Role;
import com.parking.user.entity.User;
import com.parking.user.mapper.UserMapper;
import com.parking.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock UserRepository userRepository;
	@Mock UserMapper userMapper;
	@Mock PasswordEncoder passwordEncoder;
	@InjectMocks UserServiceImpl userService;

	private UserRequest request;
	private User entity;

	@BeforeEach
	void setUp() {
		request = new UserRequest();
		request.setName("Jane");
		request.setEmail("jane@test.com");
		request.setMobile("971500000001");
		request.setPassword("plain-secret");

		entity = new User();
		entity.setEmail("jane@test.com");
		entity.setMobile("971500000001");
	}

	@Test
	void register_hashesPasswordAndDefaultsToUserRole() {
		when(userMapper.toEntity(request)).thenReturn(entity);
		when(passwordEncoder.encode("plain-secret")).thenReturn("hashed");
		when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
		when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

		userService.register(request);

		ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
		verify(userRepository).saveAndFlush(saved.capture());
		assertThat(saved.getValue().getPassword()).isEqualTo("hashed");
		assertThat(saved.getValue().getRole()).isEqualTo(Role.USER);
	}

	@Test
	void register_rejectsDuplicateEmail() {
		when(userMapper.toEntity(request)).thenReturn(entity);
		when(userRepository.existsByEmail("jane@test.com")).thenReturn(true);

		assertThatThrownBy(() -> userService.register(request))
				.isInstanceOf(EmailAlreadyRegisteredException.class);
		verify(userRepository, never()).saveAndFlush(any());
	}

	@Test
	void register_rejectsDuplicateMobile() {
		when(userMapper.toEntity(request)).thenReturn(entity);
		when(userRepository.existsByMobile("971500000001")).thenReturn(true);

		assertThatThrownBy(() -> userService.register(request))
				.isInstanceOf(MobileAlreadyRegisteredException.class);
	}

	@Test
	void register_translatesUniqueConstraintRaceIntoEmailError() {
		// Another request registered the same email between our check and the insert
		when(userMapper.toEntity(request)).thenReturn(entity);
		when(passwordEncoder.encode(any())).thenReturn("hashed");
		when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("dup",
				new org.hibernate.exception.ConstraintViolationException("dup", new SQLException(), "uk_users_email")));

		assertThatThrownBy(() -> userService.register(request))
				.isInstanceOf(EmailAlreadyRegisteredException.class);
	}

	@Test
	void getByEmail_unknownUser_throwsNotFound() {
		when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getByEmail("ghost@test.com"))
				.isInstanceOf(UserNotFoundException.class);
	}
}
