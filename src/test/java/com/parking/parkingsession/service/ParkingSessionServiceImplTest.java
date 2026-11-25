package com.parking.parkingsession.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.parking.common.exception.ParkingSessionAlreadyCompletedException;
import com.parking.common.exception.ParkingSessionNotFoundException;
import com.parking.common.exception.ParkingSpotNotAvailableException;
import com.parking.common.exception.ParkingSpotNotFoundException;
import com.parking.common.exception.VehicleAlreadyParkedException;
import com.parking.common.exception.VehicleNotFoundException;
import com.parking.parking.entity.ParkingSpot;
import com.parking.parking.entity.ParkingSpotStatus;
import com.parking.parking.entity.ParkingSpotType;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.parkingfee.service.ParkingFeeService;
import com.parking.parkingrate.service.ParkingRateService;
import com.parking.parkingsession.dto.ParkingSessionRequest;
import com.parking.parkingsession.dto.ParkingSessionResponse;
import com.parking.parkingsession.entity.ParkingSession;
import com.parking.parkingsession.entity.ParkingSessionStatus;
import com.parking.parkingsession.mapper.ParkingSessionMapper;
import com.parking.parkingsession.repository.ParkingSessionRepository;
import com.parking.user.entity.User;
import com.parking.vehicle.entity.Vehicle;
import com.parking.vehicle.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class ParkingSessionServiceImplTest {

    private static final String OWNER = "owner@test.com";

    @Mock VehicleRepository vehicleRepository;
    @Mock ParkingSpotRepository parkingSpotRepository;
    @Mock ParkingSessionRepository parkingSessionRepository;
    @Mock ParkingSessionMapper parkingSessionMapper;
    @Mock ParkingFeeService parkingFeeService;
    @Mock ParkingRateService parkingRateService;
    @InjectMocks ParkingSessionServiceImpl service;

    private Vehicle vehicle;
    private ParkingSpot spot;
    private ParkingSessionRequest request;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setEmail(OWNER);

        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setUser(owner);

        spot = new ParkingSpot();
        spot.setId(10L);
        spot.setType(ParkingSpotType.STANDARD);
        spot.setStatus(ParkingSpotStatus.AVAILABLE);

        request = new ParkingSessionRequest();
        request.setVehicleId(1L);
        request.setParkingSpotId(10L);
    }

    private void stubHappyPathLookups() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(parkingSpotRepository.findById(10L)).thenReturn(Optional.of(spot));
        when(parkingSessionMapper.toEntity(request)).thenReturn(new ParkingSession());
    }

    // ---- startSession ----

    @Test
    void start_marksSpotOccupiedAndSessionActive() {
        stubHappyPathLookups();
        when(parkingSessionRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(parkingSessionMapper.toResponse(any())).thenReturn(new ParkingSessionResponse());

        service.startSession(request, OWNER);

        assertThat(spot.getStatus()).isEqualTo(ParkingSpotStatus.OCCUPIED);
        verify(parkingSessionRepository).saveAndFlush(
                org.mockito.ArgumentMatchers.argThat(s ->
                        s.getStatus() == ParkingSessionStatus.ACTIVE
                                && s.getVehicle() == vehicle
                                && s.getParkingSpot() == spot
                                && s.getEntryTime() != null));
    }

    @Test
    void start_otherUsersVehicle_isReportedAsNotFound() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> service.startSession(request, "intruder@test.com"))
                .isInstanceOf(VehicleNotFoundException.class);
        verify(parkingSessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void start_vehicleAlreadyParked_isRejected() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(parkingSessionRepository.existsByVehicleIdAndStatus(1L, ParkingSessionStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> service.startSession(request, OWNER))
                .isInstanceOf(VehicleAlreadyParkedException.class);
    }

    @Test
    void start_unknownSpot_isNotFound() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(parkingSpotRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startSession(request, OWNER))
                .isInstanceOf(ParkingSpotNotFoundException.class);
    }

    @Test
    void start_occupiedSpot_isNotAvailable() {
        spot.setStatus(ParkingSpotStatus.OCCUPIED);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(parkingSpotRepository.findById(10L)).thenReturn(Optional.of(spot));

        assertThatThrownBy(() -> service.startSession(request, OWNER))
                .isInstanceOf(ParkingSpotNotAvailableException.class);
    }

    @Test
    void start_concurrentSpotUpdate_becomesSpotNotAvailable() {
        stubHappyPathLookups();
        when(parkingSessionRepository.saveAndFlush(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(ParkingSpot.class, 10L));

        assertThatThrownBy(() -> service.startSession(request, OWNER))
                .isInstanceOf(ParkingSpotNotAvailableException.class);
    }

    @Test
    void start_activeSessionPerSpotIndex_becomesSpotNotAvailable() {
        stubHappyPathLookups();
        when(parkingSessionRepository.saveAndFlush(any())).thenThrow(uniqueViolation("ux_active_session_per_spot"));

        assertThatThrownBy(() -> service.startSession(request, OWNER))
                .isInstanceOf(ParkingSpotNotAvailableException.class);
    }

    @Test
    void start_activeSessionPerVehicleIndex_becomesVehicleAlreadyParked() {
        stubHappyPathLookups();
        when(parkingSessionRepository.saveAndFlush(any())).thenThrow(uniqueViolation("ux_active_session_per_vehicle"));

        assertThatThrownBy(() -> service.startSession(request, OWNER))
                .isInstanceOf(VehicleAlreadyParkedException.class);
    }

    @Test
    void start_unrelatedIntegrityError_isRethrown() {
        stubHappyPathLookups();
        DataIntegrityViolationException other = uniqueViolation("some_other_constraint");
        when(parkingSessionRepository.saveAndFlush(any())).thenThrow(other);

        assertThatThrownBy(() -> service.startSession(request, OWNER)).isSameAs(other);
    }

    // ---- completeSession ----

    private ParkingSession activeSession() {
        ParkingSession session = new ParkingSession();
        session.setId(100L);
        session.setVehicle(vehicle);
        spot.setStatus(ParkingSpotStatus.OCCUPIED);
        session.setParkingSpot(spot);
        session.setStatus(ParkingSessionStatus.ACTIVE);
        session.setEntryTime(LocalDateTime.now().minusHours(2));
        return session;
    }

    @Test
    void complete_setsFeeExitTimeAndFreesSpot() {
        ParkingSession session = activeSession();
        when(parkingSessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(parkingFeeService.calculateFee(eq(ParkingSpotType.STANDARD), any(), any()))
                .thenReturn(new BigDecimal("20.00"));
        when(parkingSessionRepository.save(session)).thenReturn(session);
        when(parkingSessionMapper.toResponse(session)).thenReturn(new ParkingSessionResponse());

        service.completeSession(100L, OWNER);

        assertThat(session.getStatus()).isEqualTo(ParkingSessionStatus.COMPLETED);
        assertThat(session.getParkingFee()).isEqualByComparingTo("20.00");
        assertThat(session.getExitTime()).isNotNull();
        assertThat(spot.getStatus()).isEqualTo(ParkingSpotStatus.AVAILABLE);
    }

    @Test
    void complete_otherUsersSession_isReportedAsNotFound() {
        when(parkingSessionRepository.findById(100L)).thenReturn(Optional.of(activeSession()));

        assertThatThrownBy(() -> service.completeSession(100L, "intruder@test.com"))
                .isInstanceOf(ParkingSessionNotFoundException.class);
    }

    @Test
    void complete_alreadyCompleted_isRejected() {
        ParkingSession session = activeSession();
        session.setStatus(ParkingSessionStatus.COMPLETED);
        when(parkingSessionRepository.findById(100L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.completeSession(100L, OWNER))
                .isInstanceOf(ParkingSessionAlreadyCompletedException.class);
    }

    private static DataIntegrityViolationException uniqueViolation(String constraint) {
        return new DataIntegrityViolationException("duplicate key",
                new org.hibernate.exception.ConstraintViolationException("duplicate key", new SQLException(), constraint));
    }
}
