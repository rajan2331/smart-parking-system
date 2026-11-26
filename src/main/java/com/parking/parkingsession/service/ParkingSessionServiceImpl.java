package com.parking.parkingsession.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.common.exception.ParkingSessionAlreadyCompletedException;
import com.parking.common.exception.ParkingSessionNotFoundException;
import com.parking.common.exception.ParkingSpotNotAvailableException;
import com.parking.common.exception.ParkingSpotNotFoundException;
import com.parking.common.exception.ParkingSpotReservedException;
import com.parking.common.exception.VehicleAlreadyParkedException;
import com.parking.common.exception.VehicleNotFoundException;
import com.parking.common.util.ConstraintViolations;
import com.parking.parking.entity.ParkingSpot;
import com.parking.parking.entity.ParkingSpotStatus;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.parkingfee.service.ParkingFeeService;
import com.parking.parkingsession.dto.ParkingSessionRequest;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.parkingsession.entity.ParkingSession;
import com.parking.parkingsession.entity.ParkingSessionStatus;
import com.parking.parkingsession.mapper.ParkingSessionMapper;
import com.parking.parkingsession.repository.ParkingSessionRepository;
import com.parking.reservation.config.ReservationProperties;
import com.parking.reservation.repository.ReservationRepository;
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
    private final ParkingFeeService parkingFeeService;
    private final ReservationRepository reservationRepository;
    private final ReservationProperties reservationProperties;
    private final Clock clock;

    /** Walk-in: the driver picks any free spot without a reservation. */
    @Override
    @Transactional
    public ParkingSessionResponse startSession(ParkingSessionRequest request, String ownerEmail) {

        // Another user's vehicle is reported as "not found" so IDs can't be probed.
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .filter(v -> isOwnedBy(v, ownerEmail))
                .orElseThrow(() ->
                        new VehicleNotFoundException("Vehicle not found"));

        ParkingSpot parkingSpot = parkingSpotRepository
                .findById(request.getParkingSpotId())
                .orElseThrow(() ->
                        new ParkingSpotNotFoundException("Parking spot not found"));

        // Walk-ins have no end time, so keep them off spots that are reserved now or soon.
        LocalDateTime now = LocalDateTime.now(clock);
        if (reservationRepository.existsBlockingReservation(
                parkingSpot.getId(), now, now.plusMinutes(reservationProperties.walkInBufferMinutes()))) {
            throw new ParkingSpotReservedException(
                    "Parking spot is reserved now or within the next "
                            + reservationProperties.walkInBufferMinutes() + " minutes");
        }

        return parkingSessionMapper.toResponse(createSession(vehicle, parkingSpot));
    }

    /** Check-in: the reservation already holds the spot, so the walk-in reservation check is skipped. */
    @Override
    @Transactional
    public ParkingSession startSessionForReservation(Vehicle vehicle, ParkingSpot parkingSpot) {
        return createSession(vehicle, parkingSpot);
    }

    private ParkingSession createSession(Vehicle vehicle, ParkingSpot parkingSpot) {

        if (parkingSessionRepository.existsByVehicleIdAndStatus(
                vehicle.getId(),
                ParkingSessionStatus.ACTIVE)) {

            throw new VehicleAlreadyParkedException(
                    "Vehicle already has an active parking session");
        }

        if (parkingSpot.getStatus() != ParkingSpotStatus.AVAILABLE) {
            throw new ParkingSpotNotAvailableException(
                    "Parking spot is not available");
        }

        ParkingSession session = new ParkingSession();
        session.setVehicle(vehicle);
        session.setParkingSpot(parkingSpot);
        session.setEntryTime(LocalDateTime.now(clock));
        session.setStatus(ParkingSessionStatus.ACTIVE);

        parkingSpot.setStatus(ParkingSpotStatus.OCCUPIED);

        // The checks above can race with a concurrent request. saveAndFlush forces the SQL to run
        // here so the database guards (spot @Version, active-session unique indexes) report a
        // conflict inside this method, where we can turn it into a clear 409.
        try {
            return parkingSessionRepository.saveAndFlush(session);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ParkingSpotNotAvailableException("Parking spot was just taken by another driver");
        } catch (DataIntegrityViolationException ex) {
            String constraint = ConstraintViolations.findConstraintName(ex);
            if ("ux_active_session_per_spot".equals(constraint)) {
                throw new ParkingSpotNotAvailableException("Parking spot was just taken by another driver");
            }
            if ("ux_active_session_per_vehicle".equals(constraint)) {
                throw new VehicleAlreadyParkedException("Vehicle already has an active parking session");
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSessionResponse> getMySessions(String ownerEmail, ParkingSessionStatus status) {
        List<ParkingSession> sessions = (status == null)
                ? parkingSessionRepository.findByVehicleUserEmailOrderByEntryTimeDesc(ownerEmail)
                : parkingSessionRepository.findByVehicleUserEmailAndStatusOrderByEntryTimeDesc(ownerEmail, status);
        return sessions.stream().map(parkingSessionMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ParkingSessionResponse completeSession(Long sessionId, String ownerEmail) {

        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .filter(s -> isOwnedBy(s.getVehicle(), ownerEmail))
                .orElseThrow(() ->
                        new ParkingSessionNotFoundException(
                                "Parking session not found"));

        if (session.getStatus() == ParkingSessionStatus.COMPLETED) {
            throw new ParkingSessionAlreadyCompletedException(
                    "Parking session is already completed");
        }

        LocalDateTime exitTime = LocalDateTime.now(clock);

        BigDecimal parkingFee = parkingFeeService.calculateFee(
                session.getParkingSpot().getType(),
                session.getEntryTime(),
                exitTime
        );

        session.setExitTime(exitTime);
        session.setStatus(ParkingSessionStatus.COMPLETED);
        session.setParkingFee(parkingFee);

        ParkingSpot parkingSpot = session.getParkingSpot();
        parkingSpot.setStatus(ParkingSpotStatus.AVAILABLE);

        ParkingSession savedSession =
                parkingSessionRepository.save(session);

        return parkingSessionMapper.toResponse(savedSession);
    }

    private boolean isOwnedBy(Vehicle vehicle, String email) {
        return vehicle.getUser() != null
                && vehicle.getUser().getEmail().equalsIgnoreCase(email);
    }

}
