package com.parking.parking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.parking.parking.entity.ParkingSpot;
import com.parking.parking.entity.ParkingSpotStatus;

public interface ParkingSpotRepository
        extends JpaRepository<ParkingSpot, Long> {

    boolean existsBySpotNumber(String spotNumber);
    List<ParkingSpot> findByStatus(ParkingSpotStatus status);

    /**
     * SELECT ... FOR UPDATE: bookings for the same spot queue up behind each other. Without this,
     * two concurrent inserts can deadlock inside PostgreSQL's exclusion-constraint check.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ParkingSpot s where s.id = :id")
    Optional<ParkingSpot> findByIdForUpdate(@Param("id") Long id);
}