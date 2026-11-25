package com.parking.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    // 32+ byte keys, base64-encoded (HS256 needs at least 256 bits)
    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLTEyMzQ1Ng==";
    private static final String OTHER_SECRET = "YW5vdGhlci1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLTk4NzY1";

    @Test
    void generatedTokenRoundTripsTheEmail() {
        JwtService jwt = new JwtService(SECRET, 60_000);

        String token = jwt.generateToken("jane@test.com");

        assertThat(jwt.extractEmail(token)).isEqualTo("jane@test.com");
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String foreignToken = new JwtService(OTHER_SECRET, 60_000).generateToken("jane@test.com");

        assertThatThrownBy(() -> new JwtService(SECRET, 60_000).extractEmail(foreignToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, 60_000);
        String token = jwt.generateToken("jane@test.com");
        String tampered = token.substring(0, token.length() - 4) + "abcd";

        assertThatThrownBy(() -> jwt.extractEmail(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService alreadyExpired = new JwtService(SECRET, -1_000);
        String token = alreadyExpired.generateToken("jane@test.com");

        assertThatThrownBy(() -> alreadyExpired.extractEmail(token)).isInstanceOf(ExpiredJwtException.class);
    }
}
