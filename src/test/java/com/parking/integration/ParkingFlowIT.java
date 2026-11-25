package com.parking.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.parking.support.AbstractIntegrationTest;

class ParkingFlowIT extends AbstractIntegrationTest {

    private String sessionJson(long vehicleId, long spotId) {
        return "{\"vehicleId\":%d,\"parkingSpotId\":%d}".formatted(vehicleId, spotId);
    }

    @Test
    void fullParkingFlow_registerVehicle_park_complete_andSeeHistory() {
        TestUser driver = newUser();
        long spotId = createSpot(newAdmin());
        long vehicleId = createVehicle(driver);

        // my vehicles
        Response vehicles = get("/api/v1/vehicles", driver.token());
        assertThat(vehicles.data()).hasSize(1);
        assertThat(vehicles.data().get(0).path("id").asLong()).isEqualTo(vehicleId);

        // park
        Response started = post("/api/v1/parking-sessions", sessionJson(vehicleId, spotId), driver.token());
        assertThat(started.status()).isEqualTo(201);
        assertThat(started.data().path("vehicleId").asLong()).isEqualTo(vehicleId);
        assertThat(started.data().path("parkingSpotId").asLong()).isEqualTo(spotId);
        long sessionId = started.data().path("id").asLong();

        assertThat(get("/api/v1/parking-sessions?status=ACTIVE", driver.token()).data()).hasSize(1);

        // complete: minimum charge is one hour at the seeded STANDARD rate (10.00)
        Response completed = post("/api/v1/parking-sessions/" + sessionId + "/complete", null, driver.token());
        assertThat(completed.status()).isEqualTo(200);
        assertThat(completed.data().path("status").asString()).isEqualTo("COMPLETED");
        assertThat(completed.data().path("parkingFee").decimalValue()).isEqualByComparingTo("10.00");

        // completing twice is rejected; history shows it
        assertThat(post("/api/v1/parking-sessions/" + sessionId + "/complete", null, driver.token()).status())
                .isEqualTo(409);
        assertThat(get("/api/v1/parking-sessions?status=ACTIVE", driver.token()).data()).isEmpty();
        assertThat(get("/api/v1/parking-sessions?status=COMPLETED", driver.token()).data()).hasSize(1);
    }

    @Test
    void usersCannotSeeOrUseEachOthersVehiclesAndSessions() {
        TestUser owner = newUser();
        TestUser intruder = newUser();
        long spotId = createSpot(newAdmin());
        long vehicleId = createVehicle(owner);

        assertThat(get("/api/v1/vehicles", intruder.token()).data()).isEmpty();

        Response parkOthersCar = post("/api/v1/parking-sessions", sessionJson(vehicleId, spotId), intruder.token());
        assertThat(parkOthersCar.status()).isEqualTo(404);
        assertThat(parkOthersCar.code()).isEqualTo("VEHICLE NOT FOUND");

        long sessionId = post("/api/v1/parking-sessions", sessionJson(vehicleId, spotId), owner.token())
                .data().path("id").asLong();

        assertThat(get("/api/v1/parking-sessions", intruder.token()).data()).isEmpty();
        assertThat(post("/api/v1/parking-sessions/" + sessionId + "/complete", null, intruder.token()).status())
                .isEqualTo(404);
    }

    @Test
    void errorStatusCodes() {
        TestUser driver = newUser();
        TestUser admin = newAdmin();
        long spotId = createSpot(admin);
        long vehicleA = createVehicle(driver);
        long vehicleB = createVehicle(driver);

        // unknown spot -> 404 (used to be 500)
        assertThat(post("/api/v1/parking-sessions", sessionJson(vehicleA, 999_999), driver.token()).status())
                .isEqualTo(404);

        // occupied spot -> 409
        post("/api/v1/parking-sessions", sessionJson(vehicleA, spotId), driver.token());
        assertThat(post("/api/v1/parking-sessions", sessionJson(vehicleB, spotId), driver.token()).status())
                .isEqualTo(409);

        // duplicate plate -> 409
        String plate = unique("DUP-");
        String vehicleJson = "{\"plateNumber\":\"%s\",\"type\":\"CAR\"}".formatted(plate);
        assertThat(post("/api/v1/vehicles", vehicleJson, driver.token()).status()).isEqualTo(201);
        assertThat(post("/api/v1/vehicles", vehicleJson, driver.token()).status()).isEqualTo(409);

        // bad enum in query -> 400 JSON
        Response badStatus = get("/api/v1/parking-sessions?status=BOGUS", driver.token());
        assertThat(badStatus.status()).isEqualTo(400);
        assertThat(badStatus.code()).isEqualTo("VALIDATION_FAILED");
    }

    /** Two drivers hit "park" on the same spot at the same instant: exactly one may win. */
    @RepeatedTest(5)
    void concurrentBookingsOfTheSameSpot_onlyOneSucceeds() throws Exception {
        long spotId = createSpot(newAdmin());
        TestUser a = newUser();
        TestUser b = newUser();
        long vehicleA = createVehicle(a);
        long vehicleB = createVehicle(b);

        List<Integer> statuses = race(
                () -> post("/api/v1/parking-sessions", sessionJson(vehicleA, spotId), a.token()).status(),
                () -> post("/api/v1/parking-sessions", sessionJson(vehicleB, spotId), b.token()).status());

        assertThat(statuses).containsExactlyInAnyOrder(201, 409);
    }

    /** The same vehicle parked in two spots at the same instant: exactly one may win. */
    @RepeatedTest(5)
    void concurrentBookingsOfTheSameVehicle_onlyOneSucceeds() throws Exception {
        TestUser admin = newAdmin();
        long spot1 = createSpot(admin);
        long spot2 = createSpot(admin);
        TestUser driver = newUser();
        long vehicle = createVehicle(driver);

        List<Integer> statuses = race(
                () -> post("/api/v1/parking-sessions", sessionJson(vehicle, spot1), driver.token()).status(),
                () -> post("/api/v1/parking-sessions", sessionJson(vehicle, spot2), driver.token()).status());

        assertThat(statuses).containsExactlyInAnyOrder(201, 409);
        assertThat(get("/api/v1/parking-sessions?status=ACTIVE", driver.token()).data()).hasSize(1);
    }

    /** Runs the tasks on separate threads, released at the same moment. */
    @SafeVarargs
    private static List<Integer> race(Callable<Integer>... tasks) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tasks.length);
        CountDownLatch go = new CountDownLatch(1);
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (Callable<Integer> task : tasks) {
                futures.add(pool.submit(() -> {
                    go.await();
                    return task.call();
                }));
            }
            go.countDown();
            List<Integer> results = new ArrayList<>();
            for (Future<Integer> f : futures) {
                results.add(f.get());
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }
}
