package com.parking.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.parking.user.entity.Role;
import com.parking.user.repository.UserRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Starts the real application on a random port against a throwaway PostgreSQL container.
 * Flyway runs the real migrations, so DB rules (unique indexes, @Version) are exercised for real.
 * The container is shared by all integration test classes and removed when the JVM exits.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // Placeholders from application.properties; the datasource itself comes from @ServiceConnection
                "DB_URL=jdbc:postgresql://unused/unused",
                "DB_USERNAME=unused",
                "DB_PASSWORD=unused",
                "app.jwt.secret=aW50ZWdyYXRpb24tdGVzdC1zZWNyZXQta2V5LTEyMzQ1Njc4",
                "app.admin.email=",
                "spring.jpa.show-sql=false"
        })
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17");

    static {
        POSTGRES.start();
    }

    protected static final ObjectMapper JSON = JsonMapper.builder().build();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @LocalServerPort
    protected int port;

    @Autowired
    protected UserRepository userRepository;

    // ---------- HTTP helpers ----------

    public record Response(int status, JsonNode body) {
        public JsonNode data() {
            return body.path("data");
        }

        public String code() {
            return body.path("code").asString();
        }
    }

    protected Response get(String path, String token) {
        return send(HttpRequest.newBuilder(uri(path)).GET(), token);
    }

    protected Response post(String path, String json, String token) {
        return send(HttpRequest.newBuilder(uri(path)).POST(body(json)), token);
    }

    protected Response put(String path, String json, String token) {
        return send(HttpRequest.newBuilder(uri(path)).PUT(body(json)), token);
    }

    private Response send(HttpRequest.Builder builder, String token) {
        builder.header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        try {
            HttpResponse<String> res = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            JsonNode body = res.body() == null || res.body().isBlank()
                    ? JSON.createObjectNode()
                    : JSON.readTree(res.body());
            return new Response(res.statusCode(), body);
        } catch (Exception e) {
            throw new IllegalStateException("HTTP call failed: " + e.getMessage(), e);
        }
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private static HttpRequest.BodyPublisher body(String json) {
        return HttpRequest.BodyPublishers.ofString(json == null ? "" : json);
    }

    // ---------- Test-data helpers (unique values so tests don't collide) ----------

    protected static String unique(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    /** Registers a new user and returns a login token for them. */
    protected TestUser newUser() {
        String email = unique("user-") + "@test.com";
        String password = "secret123";
        String mobile = "9715" + (long) (Math.random() * 1_000_000_000L);
        Response reg = post("/api/v1/users", """
                {"name":"Test","email":"%s","mobile":"%s","password":"%s"}
                """.formatted(email, mobile, password), null);
        if (reg.status() != 201) {
            throw new IllegalStateException("register failed: " + reg);
        }
        return new TestUser(email, login(email, password));
    }

    /** Registers a user, promotes them to ADMIN directly in the DB, and returns a token. */
    protected TestUser newAdmin() {
        TestUser user = newUser();
        var entity = userRepository.findByEmail(user.email()).orElseThrow();
        entity.setRole(Role.ADMIN);
        userRepository.save(entity);
        return user; // role is read from the DB on every request, so the same token now has ADMIN
    }

    protected String login(String email, String password) {
        Response res = post("/api/v1/auth/login",
                "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password), null);
        if (res.status() != 200) {
            throw new IllegalStateException("login failed: " + res);
        }
        return res.data().path("accessToken").asString();
    }

    protected long createVehicle(TestUser owner) {
        Response res = post("/api/v1/vehicles",
                "{\"plateNumber\":\"%s\",\"type\":\"CAR\"}".formatted(unique("P-")), owner.token());
        if (res.status() != 201) {
            throw new IllegalStateException("create vehicle failed: " + res);
        }
        return res.data().path("id").asLong();
    }

    protected long createSpot(TestUser admin) {
        Response res = post("/api/v1/parking-spots",
                "{\"spotNumber\":\"%s\",\"type\":\"STANDARD\"}".formatted(unique("S-")), admin.token());
        if (res.status() != 201) {
            throw new IllegalStateException("create spot failed: " + res);
        }
        return res.data().path("id").asLong();
    }

    public record TestUser(String email, String token) {
    }
}
