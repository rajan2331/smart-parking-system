package com.parking.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.parking.common.exception.InvalidReservationException;
import com.parking.common.exception.ReservationConflictException;
import com.parking.common.exception.ReservationNotFoundException;
import com.parking.parking.entity.ParkingSpot;
import com.parking.parking.entity.ParkingSpotStatus;
import com.parking.parking.mapper.ParkingSpotMapper;
import com.parking.parking.repository.ParkingSpotRepository;
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

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 12, 0);
    private static final String OWNER = "owner@test.com";

    @Mock ReservationRepository reservationRepository;
    @Mock UserRepository userRepository;
    @Mock VehicleRepository vehicleRepository;
    @Mock ParkingSpotRepository parkingSpotRepository;
    @Mock ParkingSessionService parkingSessionService;
    @Mock ReservationMapper reservationMapper;
    @Mock ParkingSpotMapper parkingSpotMapper;
    @Mock ParkingSessionMapper parkingSessionMapper;
    ReservationServiceImpl service;

    private User user;
    private Vehicle vehicle;
    private ParkingSpot spot;

    @BeforeEach
    void setUp() {
        // min 30 min, max 24 h, 7 days ahead, 3 active, check-in 15 min early, 15 min grace,
        // 120 min walk-in buffer, 5 min start tolerance
        ReservationProperties props = new ReservationProperties(30, 24, 7, 3, 15, 15, 120, 5);
        Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new ReservationServiceImpl(reservationRepository, userRepository, vehicleRepository,
                parkingSpotRepository, parkingSessionService, reservationMapper, parkingSpotMapper,
                parkingSessionMapper, props, clock);

        user = new User();
        user.setId(1L);
        user.setEmail(OWNER);
        vehicle = new Vehicle();
        vehicle.setId(2L);
        vehicle.setUser(user);
        spot = new ParkingSpot();
        spot.setId(3L);
        spot.setStatus(ParkingSpotStatus.AVAILABLE);
    }

    private static ReservationRequest request(LocalDateTime start, LocalDateTime end) {
        ReservationRequest r = new ReservationRequest();
        r.setVehicleId(2L);
        r.setParkingSpotId(3L);
        r.setStartTime(start);
        r.setEndTime(end);
        return r;
    }

    private void stubLookups() {
        when(userRepository.findByEmailForUpdate(OWNER)).thenReturn(Optional.of(user));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicle));
        when(parkingSpotRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(spot));
    }

    // ---------- booking window rules (no DB needed) ----------

    @Test
    void endBeforeStart_isInvalid() {
        assertThatThrownBy(() -> service.create(request(NOW.plusHours(2), NOW.plusHours(1)), OWNER))
                .isInstanceOf(InvalidReservationException.class).hasMessageContaining("after startTime");
    }

    @Test
    void startInThePast_beyondTolerance_isInvalid() {
        assertThatThrownBy(() -> service.create(request(NOW.minusMinutes(6), NOW.plusHours(1)), OWNER))
                .isInstanceOf(InvalidReservationException.class).hasMessageContaining("past");
    }

    @Test
    void shorterThanMinimum_isInvalid() {
        assertThatThrownBy(() -> service.create(request(NOW.plusHours(1), NOW.plusHours(1).plusMinutes(29)), OWNER))
                .isInstanceOf(InvalidReservationException.class).hasMessageContaining("at least 30");
    }

    @Test
    void longerThanMaximum_isInvalid() {
        assertThatThrownBy(() -> service.create(request(NOW.plusHours(1), NOW.plusHours(25).plusMinutes(1)), OWNER))
                .isInstanceOf(InvalidReservationException.class).hasMessageContaining("at most 24");
    }

    @Test
    void tooFarAhead_isInvalid() {
        LocalDateTime start = NOW.plusDays(7).plusMinutes(1);
        assertThatThrownBy(() -> service.create(request(start, start.plusHours(1)), OWNER))
                .isInstanceOf(InvalidReservationException.class).hasMessageContaining("7 days");
    }

    // ---------- state rules ----------

    @Test
    void validBooking_isSavedAsConfirmed() {
        stubLookups();
        when(reservationRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationMapper.toResponse(any())).thenReturn(new ReservationResponse());

        service.create(request(NOW.plusHours(3), NOW.plusHours(5)), OWNER);

        verify(reservationRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(r ->
                r.getStatus() == ReservationStatus.CONFIRMED && r.getUser() == user && r.getParkingSpot() == spot));
    }

    @Test
    void otherUsersVehicle_isNotFound() {
        User someoneElse = new User();
        someoneElse.setId(99L);
        vehicle.setUser(someoneElse);
        stubLookupsWithoutSpot();

        assertThatThrownBy(() -> service.create(request(NOW.plusHours(3), NOW.plusHours(5)), OWNER))
                .isInstanceOf(com.parking.common.exception.VehicleNotFoundException.class);
    }

    private void stubLookupsWithoutSpot() {
        when(userRepository.findByEmailForUpdate(OWNER)).thenReturn(Optional.of(user));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicle));
    }

    @Test
    void spotUnderMaintenance_isConflict() {
        spot.setStatus(ParkingSpotStatus.MAINTENANCE);
        stubLookups();

        assertThatThrownBy(() -> service.create(request(NOW.plusHours(3), NOW.plusHours(5)), OWNER))
                .isInstanceOf(ReservationConflictException.class).hasMessageContaining("maintenance");
    }

    @Test
    void occupiedSpot_blocksBookingsStartingWithinBuffer_butNotLater() {
        spot.setStatus(ParkingSpotStatus.OCCUPIED);
        stubLookups();

        assertThatThrownBy(() -> service.create(request(NOW.plusMinutes(90), NOW.plusHours(3)), OWNER))
                .isInstanceOf(ReservationConflictException.class).hasMessageContaining("occupied");

        when(reservationRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        service.create(request(NOW.plusHours(3), NOW.plusHours(4)), OWNER); // beyond the 2h buffer: allowed
    }

    @Test
    void activeReservationLimit_isConflict() {
        stubLookups();
        when(reservationRepository.countByUserIdAndStatus(1L, ReservationStatus.CONFIRMED)).thenReturn(3L);

        assertThatThrownBy(() -> service.create(request(NOW.plusHours(3), NOW.plusHours(5)), OWNER))
                .isInstanceOf(ReservationConflictException.class).hasMessageContaining("maximum 3");
        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    void spotOverlapConstraint_becomesConflict() {
        stubLookups();
        when(reservationRepository.saveAndFlush(any())).thenThrow(exclusionViolation("ex_reservations_spot_overlap"));

        assertThatThrownBy(() -> service.create(request(NOW.plusHours(3), NOW.plusHours(5)), OWNER))
                .isInstanceOf(ReservationConflictException.class).hasMessageContaining("already reserved");
    }

    @Test
    void vehicleOverlapConstraint_becomesConflict() {
        stubLookups();
        when(reservationRepository.saveAndFlush(any())).thenThrow(exclusionViolation("ex_reservations_vehicle_overlap"));

        assertThatThrownBy(() -> service.create(request(NOW.plusHours(3), NOW.plusHours(5)), OWNER))
                .isInstanceOf(ReservationConflictException.class).hasMessageContaining("vehicle");
    }

    // ---------- check-in window ----------

    private Reservation confirmedStartingAt(LocalDateTime start) {
        Reservation r = new Reservation();
        r.setId(7L);
        r.setUser(user);
        r.setVehicle(vehicle);
        r.setParkingSpot(spot);
        r.setStartTime(start);
        r.setEndTime(start.plusHours(1));
        r.setStatus(ReservationStatus.CONFIRMED);
        return r;
    }

    @Test
    void checkIn_moreThan15MinutesEarly_isConflict() {
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(confirmedStartingAt(NOW.plusMinutes(16))));

        assertThatThrownBy(() -> service.checkIn(7L, OWNER))
                .isInstanceOf(ReservationConflictException.class).hasMessageContaining("opens at");
    }

    @Test
    void checkIn_moreThan15MinutesLate_isExpired() {
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(confirmedStartingAt(NOW.minusMinutes(16))));

        assertThatThrownBy(() -> service.checkIn(7L, OWNER))
                .isInstanceOf(ReservationConflictException.class).hasMessageContaining("expired");
    }

    @Test
    void checkIn_withinWindow_startsSessionAndLinksIt() {
        Reservation r = confirmedStartingAt(NOW.plusMinutes(10)); // 10 minutes early is fine
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(r));
        ParkingSession session = new ParkingSession();
        when(parkingSessionService.startSessionForReservation(vehicle, spot)).thenReturn(session);

        service.checkIn(7L, OWNER);

        assertThat(r.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
        assertThat(r.getParkingSession()).isSameAs(session);
    }

    @Test
    void checkIn_cancelledReservation_isConflict() {
        Reservation r = confirmedStartingAt(NOW);
        r.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.checkIn(7L, OWNER))
                .isInstanceOf(ReservationConflictException.class).hasMessageContaining("CANCELLED");
    }

    @Test
    void otherUsersReservation_isNotFound() {
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(confirmedStartingAt(NOW)));

        assertThatThrownBy(() -> service.cancel(7L, "intruder@test.com"))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void expireNoShows_usesStartTimeMinusGrace() {
        service.expireNoShows();

        verify(reservationRepository).expireNoShows(NOW.minusMinutes(15));
    }

    /** What PostgreSQL raises for an exclusion-constraint violation (SQLSTATE 23P01). */
    private static DataIntegrityViolationException exclusionViolation(String constraint) {
        SQLException pg = new SQLException(
                "ERROR: conflicting key value violates exclusion constraint \"" + constraint + "\"", "23P01");
        return new DataIntegrityViolationException("conflict", pg);
    }
}
