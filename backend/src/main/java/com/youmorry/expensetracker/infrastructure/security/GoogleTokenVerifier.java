package com.youmorry.expensetracker.infrastructure.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.youmorry.expensetracker.application.port.OauthTokenVerifier;
import com.youmorry.expensetracker.application.port.OauthUserInfo;
import com.youmorry.expensetracker.shared.exception.UnauthorizedException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Google ID トークンを JWKS で検証し、ユーザー情報を取得する {@link OauthTokenVerifier} の実装。 */
@Component
public class GoogleTokenVerifier implements OauthTokenVerifier {

  private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);
  private final GoogleIdTokenVerifier verifier;

  /**
   * コンストラクタ。
   *
   * @param verifier Google ID トークン検証器
   */
  public GoogleTokenVerifier(GoogleIdTokenVerifier verifier) {
    this.verifier = verifier;
  }

  @Override
  public OauthUserInfo verify(String idToken) {
    GoogleIdToken googleIdToken;
    try {
      googleIdToken = verifier.verify(idToken);
    } catch (GeneralSecurityException | IOException e) {
      log.warn("Google ID token verification failed: {}", e.getMessage());
      throw new UnauthorizedException("The Google ID token is invalid.");
    }

    if (googleIdToken == null) {
      throw new UnauthorizedException("The Google ID token is invalid.");
    }

    GoogleIdToken.Payload payload = googleIdToken.getPayload();
    return new OauthUserInfo(
        payload.getSubject(), payload.getEmail(), (String) payload.get("name"));
  }
}
