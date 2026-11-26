package com.parking.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.parking.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

	boolean existsByMobile(String mobile);
	
	Optional<User> findByEmail(String email);

	/**
	 * Locks the user row until the transaction ends (SELECT ... FOR UPDATE), so per-user checks
	 * like "max active reservations" can't be bypassed by sending requests in parallel.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from User u where u.email = :email")
	Optional<User> findByEmailForUpdate(@Param("email") String email);
	
	

}