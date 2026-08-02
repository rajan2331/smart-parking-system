package com.parking.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parking.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

}