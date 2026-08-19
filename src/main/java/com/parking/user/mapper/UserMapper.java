package com.parking.user.mapper;

import org.mapstruct.Mapper;

import com.parking.user.dto.UserRequest;
import com.parking.user.dto.UserResponse;
import com.parking.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequest request);

    UserResponse toResponse(User user);
}