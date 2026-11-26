package com.parking.reservation.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.common.exception.InvalidReservationException;
import com.parking.common.exception.ParkingSpotNotFoundException;
import com.parking.common.exception.ReservationConflictException;
import com.parking.common.exception.ReservationNotFoundException;
import com.parking.common.exception.UserNotFoundException;
import com.parking.common.exception.VehicleNotFoundException;
import com.parking.common.util.ConstraintViolations;
import com.parking.parking.dto.ParkingSpotResponse;
import com.parking.parking.entity.ParkingSpot;
import com.parking.parking.entity.ParkingSpotStatus;
import com.parking.parking.entity.ParkingSpotType;
import com.parking.parking.mapper.ParkingSpotMapper;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.parkingsession.entity.ParkingSession;
import com.parking.parkingsession.mapper.ParkingSessionMapper;
import com.parking.parkingsession.service.ParkingSessionService;
import com.parking.reservation.config.ReservationProperties;
import com.parking.reservation.dto.ReservationRequest;
import com.parking.reservation.dto.ReservationResponse;
import com.parking.reservation.entity.Reservation;
import com.parking.reservation.entity.ReservationStatus;
import com.parking.reservation.mapper.ReservationMapper;
import com.parking.reservation.repository.ReservationRepository;
import com.parking.user.entity.User;
import com.parking.user.repository.UserRepository;
import com.parking.vehicle.entity.Vehicle;
import com.parking.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    /** Reservations that hold their spot/vehicle for the window (must match the V4 exclusion constraints). */
    private static final EnumSet<ReservationStatus> LIVE =
            EnumSet.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final ParkingSessionService parkingSessionService;
    private final ReservationMapper reservationMapper;
    private final ParkingSpotMapper parkingSpotMapper;
    private final ParkingSessionMapper parkingSessionMapper;
    private final ReservationProperties props;
    private final Clock clock;

    @Override
    @Transactional
    public ReservationResponse create(ReservationRequest request, String ownerEmail) {
        LocalDateTime now = LocalDateTime.now(clock);
        validateWindow(request.getStartTime(), request.getEndTime(), now);

        // Row lock on the user: parallel requests from the same user queue up here,
        // so the "max active reservations" count below can't be raced past.
        User user = userRepository.findByEmailForUpdate(ownerEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .filter(v -> v.getUser() != null && v.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));

        // Row lock on the spot (always taken after the user lock, so lock order is consistent):
        // concurrent bookings of the same spot run one after another, and the later one gets a
        // clean overlap error instead of a deadlock inside the exclusion-constraint check.
        ParkingSpot spot = parkingSpotRepository.findByIdForUpdate(request.getParkingSpotId())
                .orElseThrow(() -> new ParkingSpotNotFoundException("Parking spot not found"));

        if (spot.getStatus() == ParkingSpotStatus.MAINTENANCE) {
            throw new ReservationConflictException("Parking spot is under maintenance");
        }
        // A walk-in is parked there now and may not leave in time for a reservation starting soon.
        if (spot.getStatus() == ParkingSpotStatus.OCCUPIED
                && request.getStartTime().isBefore(now.plusMinutes(props.walkInBufferMinutes()))) {
            throw new ReservationConflictException("Parking spot is currently occupied");
        }

        long active = reservationRepository.countByUserIdAndStatus(user.getId(), ReservationStatus.CONFIRMED);
        if (active >= props.maxActivePerUser()) {
            throw new ReservationConflictException(
                    "You already have " + active + " upcoming reservations (maximum " + props.maxActivePerUser() + ")");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setVehicle(vehicle);
        reservation.setParkingSpot(spot);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // Overlaps are enforced by PostgreSQL exclusion constraints (V4), which also covers
        // two people booking the same slot at the same instant. Flush so we can translate here.
        try {
            return reservationMapper.toResponse(reservationRepository.saveAndFlush(reservation));
        } catch (DataIntegrityViolationException ex) {
            String constraint = ConstraintViolations.findConstraintName(ex);
            if ("ex_reservations_spot_overlap".equals(constraint)) {
                throw new ReservationConflictException("Parking spot is already reserved for part of this time");
            }
            if ("ex_reservations_vehicle_overlap".equals(constraint)) {
                throw new ReservationConflictException("This vehicle already has a reservation overlapping this time");
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMine(String ownerEmail, ReservationStatus status) {
        List<Reservation> reservations = (status == null)
                ? reservationRepository.findByUserEmailOrderByStartTimeDesc(ownerEmail)
                : reservationRepository.findByUserEmailAndStatusOrderByStartTimeDesc(ownerEmail, status);
        return reservations.stream().map(reservationMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ReservationResponse cancel(Long reservationId, String ownerEmail) {
        Reservation reservation = findOwned(reservationId, ownerEmail);
        requireStatus(reservation, ReservationStatus.CONFIRMED, "cancelled");

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ParkingSessionResponse checkIn(Long reservationId, String ownerEmail) {
        Reservation reservation = findOwned(reservationId, ownerEmail);
        requireStatus(reservation, ReservationStatus.CONFIRMED, "checked in");

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime opensAt = reservation.getStartTime().minusMinutes(props.earlyCheckInMinutes());
        LocalDateTime closesAt = reservation.getStartTime().plusMinutes(props.noShowGraceMinutes());
        if (now.isBefore(opensAt)) {
            throw new ReservationConflictException("Check-in opens at " + opensAt);
        }
        if (now.isAfter(closesAt)) {
            // The expiry job will mark it EXPIRED; treat it as expired already.
            throw new ReservationConflictException("Reservation expired: check-in closed at " + closesAt);
        }

        // Normal session rules still apply (vehicle not already parked, spot actually free).
        ParkingSession session = parkingSessionService.startSessionForReservation(
                reservation.getVehicle(), reservation.getParkingSpot());

        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservation.setParkingSession(session);
        reservationRepository.save(reservation);

        return parkingSessionMapper.toResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSpotResponse> findAvailableSpots(LocalDateTime start, LocalDateTime end, ParkingSpotType type) {
        LocalDateTime now = LocalDateTime.now(clock);
        validateWindow(start, end, now);
        boolean startsSoon = start.isBefore(now.plusMinutes(props.walkInBufferMinutes()));

        return reservationRepository.findSpotsFreeForWindow(start, end, type, LIVE).stream()
                // same rule as create(): an occupied spot can't be booked for a window starting soon
                .filter(spot -> !(startsSoon && spot.getStatus() == ParkingSpotStatus.OCCUPIED))
                .map(parkingSpotMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public int expireNoShows() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minusMinutes(props.noShowGraceMinutes());
        return reservationRepository.expireNoShows(cutoff);
    }

    // ---------- helpers ----------

    private void validateWindow(LocalDateTime start, LocalDateTime end, LocalDateTime now) {
        if (start == null || end == null) {
            throw new InvalidReservationException("startTime and endTime are required");
        }
        if (!end.isAfter(start)) {
            throw new InvalidReservationException("endTime must be after startTime");
        }
        if (start.isBefore(now.minusMinutes(props.startTimeToleranceMinutes()))) {
            throw new InvalidReservationException("startTime is in the past");
        }
        if (start.isAfter(now.plusDays(props.maxDaysAhead()))) {
            throw new InvalidReservationException(
                    "Reservations can be made at most " + props.maxDaysAhead() + " days ahead");
        }
        Duration duration = Duration.between(start, end);
        if (duration.compareTo(Duration.ofMinutes(props.minDurationMinutes())) < 0) {
            throw new InvalidReservationException(
                    "Reservation must be at least " + props.minDurationMinutes() + " minutes");
        }
        if (duration.compareTo(Duration.ofHours(props.maxDurationHours())) > 0) {
            throw new InvalidReservationException(
                    "Reservation can be at most " + props.maxDurationHours() + " hours");
        }
    }

    /** Another user's reservation is reported as "not found" so IDs can't be probed. */
    private Reservation findOwned(Long reservationId, String ownerEmail) {
        return reservationRepository.findById(reservationId)
                .filter(r -> r.getUser().getEmail().equalsIgnoreCase(ownerEmail))
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
    }

    private static void requireStatus(Reservation reservation, ReservationStatus expected, String action) {
        if (reservation.getStatus() != expected) {
            throw new ReservationConflictException(
                    "Reservation is " + reservation.getStatus() + " and can't be " + action);
        }
    }
}
