package com.parking.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.parking.reservation.service.ReservationService;
import com.parking.support.AbstractIntegrationTest;

class ReservationIT extends AbstractIntegrationTest {

    @Autowired ReservationService reservationService;
    @Autowired JdbcTemplate jdbc;

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private static String json(long vehicleId, long spotId, LocalDateTime start, LocalDateTime end) {
        return "{\"vehicleId\":%d,\"parkingSpotId\":%d,\"startTime\":\"%s\",\"endTime\":\"%s\"}"
                .formatted(vehicleId, spotId, start, end);
    }

    private Response book(TestUser user, long vehicleId, long spotId, LocalDateTime start, LocalDateTime end) {
        return post("/api/v1/reservations", json(vehicleId, spotId, start, end), user.token());
    }

    @Test
    void bookListCancel_andTheSlotBecomesFreeAgain() {
        long spot = createSpot(newAdmin());
        TestUser driver = newUser();
        long vehicle = createVehicle(driver);
        LocalDateTime start = now().plusDays(1);

        Response booked = book(driver, vehicle, spot, start, start.plusHours(2));
        assertThat(booked.status()).isEqualTo(201);
        assertThat(booked.data().path("status").asString()).isEqualTo("CONFIRMED");
        long id = booked.data().path("id").asLong();

        assertThat(get("/api/v1/reservations?status=CONFIRMED", driver.token()).data()).hasSize(1);

        Response cancelled = post("/api/v1/reservations/" + id + "/cancel", null, driver.token());
        assertThat(cancelled.status()).isEqualTo(200);
        assertThat(cancelled.data().path("status").asString()).isEqualTo("CANCELLED");

        // cancelled reservations don't hold the spot
        assertThat(book(driver, vehicle, spot, start, start.plusHours(2)).status()).isEqualTo(201);
        // can't cancel twice
        assertThat(post("/api/v1/reservations/" + id + "/cancel", null, driver.token()).status()).isEqualTo(409);
    }

    @Test
    void overlappingBookingsOfTheSameSpotAreRejected_backToBackIsFine() {
        long spot = createSpot(newAdmin());
        TestUser a = newUser();
        TestUser b = newUser();
        long vehicleA = createVehicle(a);
        long vehicleB = createVehicle(b);
        LocalDateTime start = now().plusDays(1);

        assertThat(book(a, vehicleA, spot, start, start.plusHours(2)).status()).isEqualTo(201);

        Response overlap = book(b, vehicleB, spot, start.plusHours(1), start.plusHours(3));
        assertThat(overlap.status()).isEqualTo(409);
        assertThat(overlap.code()).isEqualTo("RESERVATION_CONFLICT");

        // ends exactly when the next one starts -> allowed
        assertThat(book(b, vehicleB, spot, start.plusHours(2), start.plusHours(3)).status()).isEqualTo(201);
    }

    @Test
    void sameVehicleCantHoldOverlappingBookingsOnDifferentSpots() {
        TestUser admin = newAdmin();
        long spot1 = createSpot(admin);
        long spot2 = createSpot(admin);
        TestUser driver = newUser();
        long vehicle = createVehicle(driver);
        LocalDateTime start = now().plusDays(1);

        assertThat(book(driver, vehicle, spot1, start, start.plusHours(2)).status()).isEqualTo(201);
        assertThat(book(driver, vehicle, spot2, start.plusHours(1), start.plusHours(3)).status()).isEqualTo(409);
    }

    @Test
    void bookingRulesAreValidated() {
        long spot = createSpot(newAdmin());
        TestUser driver = newUser();
        long vehicle = createVehicle(driver);
        LocalDateTime start = now().plusHours(3);

        assertThat(book(driver, vehicle, spot, start, start.plusMinutes(20)).code()).isEqualTo("INVALID_RESERVATION");
        assertThat(book(driver, vehicle, spot, start, start.plusHours(25)).status()).isEqualTo(400);
        assertThat(book(driver, vehicle, spot, now().plusDays(8), now().plusDays(8).plusHours(1)).status()).isEqualTo(400);
        assertThat(book(driver, vehicle, spot, start, start.minusHours(1)).status()).isEqualTo(400);
        assertThat(book(driver, vehicle, spot, now().minusHours(1), now().plusHours(1)).status()).isEqualTo(400);
    }

    @Test
    void atMostThreeUpcomingReservationsPerUser() {
        TestUser admin = newAdmin();
        TestUser driver = newUser();
        long vehicle = createVehicle(driver);
        LocalDateTime start = now().plusDays(1);

        for (int i = 0; i < 3; i++) {
            LocalDateTime s = start.plusHours(i * 2L);
            assertThat(book(driver, vehicle, createSpot(admin), s, s.plusHours(1)).status()).isEqualTo(201);
        }
        Response fourth = book(driver, vehicle, createSpot(admin), start.plusHours(8), start.plusHours(9));
        assertThat(fourth.status()).isEqualTo(409);
        assertThat(fourth.body().path("message").asString()).contains("maximum 3");
    }

    @Test
    void walkInsAreBlockedOnlyWhenTheReservationStartsWithinTwoHours() {
        TestUser admin = newAdmin();
        TestUser reserver = newUser();
        TestUser walkIn = newUser();
        long reserverCar = createVehicle(reserver);
        long walkInCar = createVehicle(walkIn);
        long soonSpot = createSpot(admin);
        long laterSpot = createSpot(admin);

        book(reserver, reserverCar, soonSpot, now().plusHours(1), now().plusHours(2));   // within 2h buffer
        book(reserver, reserverCar, laterSpot, now().plusHours(3), now().plusHours(4));  // beyond buffer

        Response blocked = post("/api/v1/parking-sessions",
                "{\"vehicleId\":%d,\"parkingSpotId\":%d}".formatted(walkInCar, soonSpot), walkIn.token());
        assertThat(blocked.status()).isEqualTo(409);
        assertThat(blocked.code()).isEqualTo("PARKING_SPOT_RESERVED");

        // the soon-reserved spot is hidden from walk-ins; the later one is still offered
        List<Long> available = new ArrayList<>();
        get("/api/v1/parking-spots/available", walkIn.token()).data().forEach(s -> available.add(s.path("id").asLong()));
        assertThat(available).doesNotContain(soonSpot).contains(laterSpot);

        Response allowed = post("/api/v1/parking-sessions",
                "{\"vehicleId\":%d,\"parkingSpotId\":%d}".formatted(walkInCar, laterSpot), walkIn.token());
        assertThat(allowed.status()).isEqualTo(201);
    }

    @Test
    void checkInStartsASessionOnTheReservedSpot() {
        long spot = createSpot(newAdmin());
        TestUser driver = newUser();
        long vehicle = createVehicle(driver);
        LocalDateTime start = now().plusMinutes(5); // within the 15-minute early check-in window

        long id = book(driver, vehicle, spot, start, start.plusHours(1)).data().path("id").asLong();

        Response session = post("/api/v1/reservations/" + id + "/check-in", null, driver.token());
        assertThat(session.status()).isEqualTo(201);
        assertThat(session.data().path("status").asString()).isEqualTo("ACTIVE");
        assertThat(session.data().path("parkingSpotId").asLong()).isEqualTo(spot);

        Response reservation = get("/api/v1/reservations", driver.token());
        assertThat(reservation.data().get(0).path("status").asString()).isEqualTo("CHECKED_IN");
        assertThat(reservation.data().get(0).path("parkingSessionId").asLong())
                .isEqualTo(session.data().path("id").asLong());

        // can't check in twice; the session completes normally
        assertThat(post("/api/v1/reservations/" + id + "/check-in", null, driver.token()).status()).isEqualTo(409);
        long sessionId = session.data().path("id").asLong();
        assertThat(post("/api/v1/parking-sessions/" + sessionId + "/complete", null, driver.token()).status())
                .isEqualTo(200);
    }

    @Test
    void checkInTooEarlyIsRejected() {
        long spot = createSpot(newAdmin());
        TestUser driver = newUser();
        long vehicle = createVehicle(driver);
        LocalDateTime start = now().plusHours(3);
        long id = book(driver, vehicle, spot, start, start.plusHours(1)).data().path("id").asLong();

        Response early = post("/api/v1/reservations/" + id + "/check-in", null, driver.token());
        assertThat(early.status()).isEqualTo(409);
        assertThat(early.body().path("message").asString()).contains("Check-in opens at");
    }

    @Test
    void noShowsExpireAndFreeTheSpot() {
        long spot = createSpot(newAdmin());
        TestUser driver = newUser();
        long vehicle = createVehicle(driver);
        LocalDateTime start = now().plusHours(3);
        long id = book(driver, vehicle, spot, start, start.plusHours(1)).data().path("id").asLong();

        // Pretend it started 20 minutes ago (past the 15-minute grace) and run the expiry job
        jdbc.update("update reservations set start_time = ?, end_time = ? where id = ?",
                now().minusMinutes(20), now().plusMinutes(40), id);
        assertThat(reservationService.expireNoShows()).isGreaterThanOrEqualTo(1);

        assertThat(get("/api/v1/reservations", driver.token()).data().get(0).path("status").asString())
                .isEqualTo("EXPIRED");
        assertThat(post("/api/v1/reservations/" + id + "/check-in", null, driver.token()).status()).isEqualTo(409);

        // expired reservations don't block walk-ins
        TestUser walkIn = newUser();
        assertThat(post("/api/v1/parking-sessions",
                "{\"vehicleId\":%d,\"parkingSpotId\":%d}".formatted(createVehicle(walkIn), spot),
                walkIn.token()).status()).isEqualTo(201);
    }

    @Test
    void availableSpotsSearchExcludesBookedSpots() {
        TestUser admin = newAdmin();
        long booked = createSpot(admin);
        long free = createSpot(admin);
        TestUser driver = newUser();
        long vehicle = createVehicle(driver);
        LocalDateTime start = now().plusDays(2);
        book(driver, vehicle, booked, start, start.plusHours(2));

        Response res = get("/api/v1/reservations/available-spots?start=%s&end=%s&type=STANDARD"
                .formatted(start.plusHours(1), start.plusHours(3)), driver.token());

        assertThat(res.status()).isEqualTo(200);
        List<Long> ids = new ArrayList<>();
        res.data().forEach(s -> ids.add(s.path("id").asLong()));
        assertThat(ids).contains(free).doesNotContain(booked);
    }

    @Test
    void usersCantSeeOrTouchOthersReservations() {
        long spot = createSpot(newAdmin());
        TestUser owner = newUser();
        TestUser intruder = newUser();
        long vehicle = createVehicle(owner);
        LocalDateTime start = now().plusDays(1);
        long id = book(owner, vehicle, spot, start, start.plusHours(1)).data().path("id").asLong();

        assertThat(get("/api/v1/reservations", intruder.token()).data()).isEmpty();
        assertThat(post("/api/v1/reservations/" + id + "/cancel", null, intruder.token()).status()).isEqualTo(404);
        assertThat(post("/api/v1/reservations/" + id + "/check-in", null, intruder.token()).status()).isEqualTo(404);
        // and can't book with someone else's vehicle
        assertThat(book(intruder, vehicle, spot, start.plusHours(2), start.plusHours(3)).status()).isEqualTo(404);
    }

    /**
     * Two drivers book the same spot and window at the same instant: exactly one wins, the other
     * gets a clean 409. (Before the spot row lock this intermittently deadlocked -> 500, so it is
     * repeated many times.)
     */
    @RepeatedTest(20)
    void concurrentBookingsOfTheSameWindow_onlyOneSucceeds() throws Exception {
        long spot = createSpot(newAdmin());
        TestUser a = newUser();
        TestUser b = newUser();
        long vehicleA = createVehicle(a);
        long vehicleB = createVehicle(b);
        LocalDateTime start = now().plusDays(1);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch go = new CountDownLatch(1);
        try {
            Future<Integer> fa = pool.submit(() -> { go.await(); return book(a, vehicleA, spot, start, start.plusHours(1)).status(); });
            Future<Integer> fb = pool.submit(() -> { go.await(); return book(b, vehicleB, spot, start, start.plusHours(1)).status(); });
            go.countDown();
            assertThat(List.of(fa.get(), fb.get())).containsExactlyInAnyOrder(201, 409);
        } finally {
            pool.shutdownNow();
        }
    }
}
