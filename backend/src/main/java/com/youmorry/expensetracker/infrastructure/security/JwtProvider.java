package com.youmorry.expensetracker.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.youmorry.expensetracker.application.port.JwtTokenGenerator;
import com.youmorry.expensetracker.domain.model.user.UserId;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** HS256 で署名する {@link JwtTokenGenerator} の実装。sub・email・iat・exp をペイロードに含める。 */
@Component
public class JwtProvider implements JwtTokenGenerator {

  private final Algorithm algorithm;
  private final long expirationHours;

  /**
   * 指定された秘密鍵と有効期限で JWT プロバイダーを構成する。
   *
   * @param secret JWT 署名用の秘密鍵
   * @param expirationHours JWT の有効期限（時間単位）
   */
  public JwtProvider(
      @Value("${app.auth.jwt-secret}") String secret,
      @Value("${app.auth.jwt-expiration-hours}") long expirationHours) {
    this.algorithm = Algorithm.HMAC256(secret);
    this.expirationHours = expirationHours;
  }

  /**
   * 指定されたユーザー情報から JWT アクセストークンを生成する。
   *
   * @param userId ユーザー ID
   * @param email メールアドレス
   * @return JWT アクセストークン文字列
   */
  @Override
  public String generateToken(UserId userId, String email) {
    Instant now = Instant.now();
    return JWT.create()
        .withSubject(String.valueOf(userId.value()))
        .withClaim("email", email)
        .withIssuedAt(now)
        .withExpiresAt(now.plus(expirationHours, ChronoUnit.HOURS))
        .sign(algorithm);
  }
}
