package com.youmorry.expensetracker.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.youmorry.expensetracker.domain.model.user.User;
import com.youmorry.expensetracker.domain.model.user.UserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

  private static final String SECRET = "test-secret-key-at-least-32-bytes-long!!";
  private static final long EXPIRATION_HOURS = 24;

  private final JwtProvider jwtProvider = new JwtProvider(SECRET, EXPIRATION_HOURS);

  @Test
  void generateToken_returnsValidJwt() {
    var user =
        new User(
            new UserId(42L),
            "google-123",
            "test@gmail.com",
            "Test User",
            "JPY",
            Instant.parse("2026-01-01T00:00:00Z"));

    String token = jwtProvider.generateToken(user);

    assertNotNull(token);
    DecodedJWT decoded = JWT.decode(token);
    assertEquals("42", decoded.getSubject());
    assertEquals("test@gmail.com", decoded.getClaim("email").asString());
    assertNotNull(decoded.getIssuedAt());
    assertNotNull(decoded.getExpiresAt());
  }

  @Test
  void generateToken_setsExpirationTo24Hours() {
    var user =
        new User(
            new UserId(1L),
            "google-456",
            "user@gmail.com",
            "User",
            "USD",
            Instant.parse("2026-01-01T00:00:00Z"));

    String token = jwtProvider.generateToken(user);

    DecodedJWT decoded = JWT.decode(token);
    long diffSeconds =
        decoded.getExpiresAt().getTime() / 1000 - decoded.getIssuedAt().getTime() / 1000;
    assertEquals(EXPIRATION_HOURS * 3600, diffSeconds);
  }
}
