package com.youmorry.expensetracker.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.youmorry.expensetracker.domain.model.user.UserId;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

  private static final String SECRET = "test-secret-key-at-least-32-bytes-long!!";
  private static final long EXPIRATION_HOURS = 24;

  private final JwtProvider jwtProvider = new JwtProvider(SECRET, EXPIRATION_HOURS);

  @Test
  void generateToken_returnsValidJwt() {
    var userId = new UserId(42L);
    var email = "test@gmail.com";

    String token = jwtProvider.generateToken(userId, email);

    assertNotNull(token);
    DecodedJWT decoded = JWT.decode(token);
    assertEquals("42", decoded.getSubject());
    assertEquals("test@gmail.com", decoded.getClaim("email").asString());
    assertNotNull(decoded.getIssuedAt());
    assertNotNull(decoded.getExpiresAt());
  }

  @Test
  void generateToken_setsExpirationTo24Hours() {
    var userId = new UserId(1L);
    var email = "user@gmail.com";

    String token = jwtProvider.generateToken(userId, email);

    DecodedJWT decoded = JWT.decode(token);
    long diffSeconds =
        decoded.getExpiresAt().getTime() / 1000 - decoded.getIssuedAt().getTime() / 1000;
    assertEquals(EXPIRATION_HOURS * 3600, diffSeconds);
  }
}
