package com.parking.parkingsession.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.parking.parking.entity.ParkingSpot;
import com.parking.vehicle.entity.Vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parking_sessions")
@Data
@NoArgsConstructor
public class ParkingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "VEHICLE_ID", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PARKING_SPOT_ID", nullable = false)
    private ParkingSpot parkingSpot;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParkingSessionStatus status;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal parkingFee;
}
