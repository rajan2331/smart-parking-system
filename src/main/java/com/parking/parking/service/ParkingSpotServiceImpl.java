package com.parking.parking.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.common.exception.ParkingSpotAlreadyRegisteredException;
import com.parking.parking.dto.ParkingSpotRequest;
import com.parking.parking.dto.ParkingSpotResponse;
import com.parking.parking.entity.ParkingSpot;
import com.parking.parking.entity.ParkingSpotStatus;
import com.parking.parking.mapper.ParkingSpotMapper;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.reservation.config.ReservationProperties;
import com.parking.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParkingSpotServiceImpl implements ParkingSpotService {

    private final ParkingSpotRepository parkingSpotRepository;
    private final ParkingSpotMapper parkingSpotMapper;
    private final ReservationRepository reservationRepository;
    private final ReservationProperties reservationProperties;
    private final Clock clock;

    @Override
    @Transactional
    public ParkingSpotResponse create(ParkingSpotRequest request) {

        if (parkingSpotRepository.existsBySpotNumber(request.getSpotNumber())) {
            throw new ParkingSpotAlreadyRegisteredException(
                    "Parking spot already exists"
            );
        }

        ParkingSpot parkingSpot = parkingSpotMapper.toEntity(request);

        parkingSpot.setStatus(ParkingSpotStatus.AVAILABLE);
        ParkingSpot savedSpot = parkingSpotRepository.save(parkingSpot);

        return parkingSpotMapper.toResponse(savedSpot);
    }

    /** Spots a walk-in can take right now: free, and not reserved now or within the walk-in buffer. */
    @Override
    @Transactional(readOnly = true)
    public List<ParkingSpotResponse> getAvailableSpots() {

        LocalDateTime now = LocalDateTime.now(clock);
        Set<Long> reservedSoon = new HashSet<>(reservationRepository.findSpotIdsBlockedForWalkIns(
                now, now.plusMinutes(reservationProperties.walkInBufferMinutes())));

        return parkingSpotRepository
                .findByStatus(ParkingSpotStatus.AVAILABLE)
                .stream()
                .filter(spot -> !reservedSoon.contains(spot.getId()))
                .map(parkingSpotMapper::toResponse)
                .toList();
    }
}
