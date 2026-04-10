package com.youmorry.expensetracker.auth.infrastructure;

import com.youmorry.expensetracker.auth.application.JwtTokenGenerator;
import com.youmorry.expensetracker.user.domain.UserId;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

/** HS256 で署名する {@link JwtTokenGenerator} の実装。sub・email・iat・exp をペイロードに含める。 */
@Component
public class JwtProvider implements JwtTokenGenerator {

  private final NimbusJwtEncoder encoder;
  private final String issuer;
  private final long expirationHours;

  /**
   * 指定された秘密鍵、issuer、有効期限で JWT プロバイダーを構成する。
   *
   * @param secret JWT 署名用の秘密鍵
   * @param issuer JWT の発行者（iss クレーム）
   * @param expirationHours JWT の有効期限（時間単位）
   */
  public JwtProvider(
      @Value("${app.auth.jwt-secret}") String secret,
      @Value("${app.auth.jwt-issuer}") String issuer,
      @Value("${app.auth.jwt-expiration-hours}") long expirationHours) {
    SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");
    this.encoder = NimbusJwtEncoder.withSecretKey(key).build();
    this.issuer = issuer;
    this.expirationHours = expirationHours;
  }

  @Override
  public String generateToken(UserId userId, String email) {
    Instant now = Instant.now();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(String.valueOf(userId.value()))
            .claim("email", email)
            .issuedAt(now)
            .expiresAt(now.plus(expirationHours, ChronoUnit.HOURS))
            .build();
    return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
