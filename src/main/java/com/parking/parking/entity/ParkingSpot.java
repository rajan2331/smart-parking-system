package com.parking.parking.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SPOT_NUMBER", nullable = false, unique = true)
    private String spotNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParkingSpotType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParkingSpotStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
