package com.youmorry.expensetracker.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.youmorry.expensetracker.domain.model.user.UserId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

class JwtProviderTest {

  // 32 bytes of key material, Base64-encoded
  private static final String SECRET =
      Base64.getEncoder().encodeToString("test-secret-key-at-least-32-byte".getBytes());
  private static final String ISSUER = "https://test.example.com";
  private static final long EXPIRATION_HOURS = 24;

  private final JwtProvider jwtProvider = new JwtProvider(SECRET, ISSUER, EXPIRATION_HOURS);

  private final NimbusJwtDecoder decoder =
      NimbusJwtDecoder.withSecretKey(
              new SecretKeySpec(Base64.getDecoder().decode(SECRET), "HmacSHA256"))
          .build();

  @Test
  void generateToken_returnsValidJwt() {
    var userId = new UserId(42L);
    var email = "test@gmail.com";

    String token = jwtProvider.generateToken(userId, email);

    assertNotNull(token);
    Jwt decoded = decoder.decode(token);
    assertEquals("42", decoded.getSubject());
    assertEquals("test@gmail.com", decoded.getClaimAsString("email"));
    assertNotNull(decoded.getIssuedAt());
    assertNotNull(decoded.getExpiresAt());
    assertEquals(ISSUER, decoded.getClaimAsString("iss"));
  }

  @Test
  void generateToken_setsExpirationTo24Hours() {
    var userId = new UserId(1L);
    var email = "user@gmail.com";

    String token = jwtProvider.generateToken(userId, email);

    Jwt decoded = decoder.decode(token);
    long diffSeconds = ChronoUnit.SECONDS.between(decoded.getIssuedAt(), decoded.getExpiresAt());
    assertEquals(EXPIRATION_HOURS * 3600, diffSeconds);
  }
}
