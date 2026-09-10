package com.parking.parking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.common.exception.ParkingSpotAlreadyRegisteredException;
import com.parking.parking.dto.ParkingSpotRequest;
import com.parking.parking.dto.ParkingSpotResponse;
import com.parking.parking.entity.ParkingSpot;
import com.parking.parking.entity.ParkingSpotStatus;
import com.parking.parking.mapper.ParkingSpotMapper;
import com.parking.parking.repository.ParkingSpotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParkingSpotServiceImpl implements ParkingSpotService {

    private final ParkingSpotRepository parkingSpotRepository;
    private final ParkingSpotMapper parkingSpotMapper;

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
    
    @Override
    @Transactional(readOnly = true)
    public List<ParkingSpotResponse> getAvailableSpots() {

        return parkingSpotRepository
                .findByStatus(ParkingSpotStatus.AVAILABLE)
                .stream()
                .map(parkingSpotMapper::toResponse)
                .toList();
    }
}