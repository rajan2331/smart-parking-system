package com.parking.parkingsession.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.common.exception.ParkingSessionAlreadyCompletedException;
import com.parking.common.exception.ParkingSessionNotFoundException;
import com.parking.common.exception.ParkingSpotNotAvailableException;
import com.parking.common.exception.VehicleAlreadyParkedException;
import com.parking.common.exception.VehicleNotFoundException;
import com.parking.parking.entity.ParkingSpot;
import com.parking.parking.entity.ParkingSpotStatus;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.parkingsession.dto.ParkingSessionRequest;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.parkingsession.entity.ParkingSession;
import com.parking.parkingsession.entity.ParkingSessionStatus;
import com.parking.parkingsession.mapper.ParkingSessionMapper;
import com.parking.parkingsession.repository.ParkingSessionRepository;
import com.parking.vehicle.entity.Vehicle;
import com.parking.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParkingSessionServiceImpl implements ParkingSessionService {

    private final VehicleRepository vehicleRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingSessionMapper parkingSessionMapper;

    @Override
    @Transactional
    public ParkingSessionResponse startSession(ParkingSessionRequest request) {

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() ->
                        new VehicleNotFoundException("Vehicle not found"));

        if (parkingSessionRepository.existsByVehicleIdAndStatus(
                request.getVehicleId(),
                ParkingSessionStatus.ACTIVE)) {

            throw new VehicleAlreadyParkedException(
                    "Vehicle already has an active parking session");
        }
        ParkingSpot parkingSpot = parkingSpotRepository
                .findById(request.getParkingSpotId())
                .orElseThrow(() ->
                        new RuntimeException("Parking spot not found"));

      
        if (parkingSpot.getStatus() != ParkingSpotStatus.AVAILABLE) {
            throw new ParkingSpotNotAvailableException(
                    "Parking spot is not available");
        }

        ParkingSession session = parkingSessionMapper.toEntity(request);

        session.setVehicle(vehicle);
        session.setParkingSpot(parkingSpot);
        session.setEntryTime(LocalDateTime.now());
        session.setStatus(ParkingSessionStatus.ACTIVE);

        parkingSpot.setStatus(ParkingSpotStatus.OCCUPIED);

        ParkingSession savedSession =
                parkingSessionRepository.save(session);

        return parkingSessionMapper.toResponse(savedSession);
    }
    
    @Override
    @Transactional
    public ParkingSessionResponse completeSession(Long sessionId) {

        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new ParkingSessionNotFoundException(
                                "Parking session not found"));

        if (session.getStatus() == ParkingSessionStatus.COMPLETED) {
            throw new ParkingSessionAlreadyCompletedException(
                    "Parking session is already completed");
        }

        session.setExitTime(LocalDateTime.now());
        session.setStatus(ParkingSessionStatus.COMPLETED);

        ParkingSpot parkingSpot = session.getParkingSpot();
        parkingSpot.setStatus(ParkingSpotStatus.AVAILABLE);

        ParkingSession savedSession =
                parkingSessionRepository.save(session);

        return parkingSessionMapper.toResponse(savedSession);
    }
}