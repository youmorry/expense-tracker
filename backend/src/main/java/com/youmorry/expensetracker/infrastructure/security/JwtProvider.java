package com.youmorry.expensetracker.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.youmorry.expensetracker.domain.model.user.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** JWT の生成を担う。HS256 で署名し、sub・email・iat・exp をペイロードに含める。 */
@Component
public class JwtProvider {

  private final Algorithm algorithm;
  private final long expirationHours;

  /** 指定された秘密鍵と有効期限で JWT プロバイダーを構成する。 */
  public JwtProvider(
      @Value("${app.auth.jwt-secret}") String secret,
      @Value("${app.auth.jwt-expiration-hours}") long expirationHours) {
    this.algorithm = Algorithm.HMAC256(secret);
    this.expirationHours = expirationHours;
  }

  /** ユーザー情報から JWT アクセストークンを生成する。 */
  public String generateToken(User user) {
    Instant now = Instant.now();
    return JWT.create()
        .withSubject(String.valueOf(user.id().value()))
        .withClaim("email", user.email())
        .withIssuedAt(now)
        .withExpiresAt(now.plus(expirationHours, ChronoUnit.HOURS))
        .sign(algorithm);
  }
}
