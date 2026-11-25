package com.parking.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.parking.support.AbstractIntegrationTest;

class AuthAndSecurityIT extends AbstractIntegrationTest {

    @Test
    void registerThenLoginThenFetchOwnProfile() {
        TestUser jane = newUser();

        Response me = get("/api/v1/users/me", jane.token());

        assertThat(me.status()).isEqualTo(200);
        assertThat(me.data().path("email").asString()).isEqualTo(jane.email());
        assertThat(me.data().path("role").asString()).isEqualTo("USER");
        assertThat(me.data().has("password")).isFalse();
    }

    @Test
    void wrongPasswordAndUnknownEmailGetTheSameResponse() {
        TestUser jane = newUser();

        Response wrongPassword = post("/api/v1/auth/login",
                "{\"email\":\"%s\",\"password\":\"nope\"}".formatted(jane.email()), null);
        Response unknownEmail = post("/api/v1/auth/login",
                "{\"email\":\"ghost@test.com\",\"password\":\"nope\"}", null);

        assertThat(wrongPassword.status()).isEqualTo(401);
        assertThat(unknownEmail.status()).isEqualTo(401);
        assertThat(wrongPassword.body()).isEqualTo(unknownEmail.body());
        assertThat(wrongPassword.code()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void protectedEndpointsRequireAValidToken() {
        assertThat(get("/api/v1/vehicles", null).status()).isEqualTo(401);
        assertThat(get("/api/v1/vehicles", "not-a-jwt").status()).isEqualTo(401);
        assertThat(get("/api/v1/vehicles", null).code()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    void registerCannotGrantAdmin() {
        String email = unique("sneaky-") + "@test.com";
        post("/api/v1/users", """
                {"name":"x","email":"%s","mobile":"%s","password":"secret123","role":"ADMIN"}
                """.formatted(email, "9716" + System.nanoTime() % 1_000_000_000L), null);

        String token = login(email, "secret123");

        assertThat(get("/api/v1/users/me", token).data().path("role").asString()).isEqualTo("USER");
    }

    @Test
    void onlyAdminsCanCreateSpotsAndChangeRates() {
        TestUser user = newUser();
        TestUser admin = newAdmin();
        String spotJson = "{\"spotNumber\":\"%s\",\"type\":\"EV\"}".formatted(unique("S-"));

        assertThat(post("/api/v1/parking-spots", spotJson, user.token()).status()).isEqualTo(403);
        assertThat(post("/api/v1/parking-spots", spotJson, admin.token()).status()).isEqualTo(201);

        assertThat(put("/api/v1/parking-rates/HANDICAPPED", "{\"hourlyRate\":7.50}", user.token()).status())
                .isEqualTo(403);
        Response updated = put("/api/v1/parking-rates/HANDICAPPED", "{\"hourlyRate\":7.50}", admin.token());
        assertThat(updated.status()).isEqualTo(200);
        assertThat(updated.data().path("hourlyRate").decimalValue()).isEqualByComparingTo("7.50");

        // restore the seeded value for other tests
        put("/api/v1/parking-rates/HANDICAPPED", "{\"hourlyRate\":5.00}", admin.token());
    }

    @Test
    void invalidRateAndUnknownSpotTypeAreRejected() {
        TestUser admin = newAdmin();

        assertThat(put("/api/v1/parking-rates/EV", "{\"hourlyRate\":-1}", admin.token()).status()).isEqualTo(400);
        assertThat(put("/api/v1/parking-rates/TRUCK", "{\"hourlyRate\":1}", admin.token()).status()).isEqualTo(400);
    }

    @Test
    void swaggerDocsArePublic() {
        Response docs = get("/v3/api-docs", null);

        assertThat(docs.status()).isEqualTo(200);
        assertThat(docs.body().path("paths").has("/api/v1/auth/login")).isTrue();
    }
}
