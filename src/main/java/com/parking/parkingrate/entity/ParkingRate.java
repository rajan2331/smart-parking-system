package com.parking.parkingrate.entity;


import java.math.BigDecimal;

import com.parking.parking.entity.ParkingSpotType;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parking_rates")
@Data
@NoArgsConstructor
public class ParkingRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private ParkingSpotType spotType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;
}