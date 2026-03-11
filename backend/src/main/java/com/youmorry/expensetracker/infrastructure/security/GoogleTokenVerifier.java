package com.youmorry.expensetracker.infrastructure.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.youmorry.expensetracker.shared.exception.UnauthorizedException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Google ID トークンを JWKS で検証し、クレームを取得する。 */
@Component
public class GoogleTokenVerifier {

  private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);
  private final GoogleIdTokenVerifier verifier;

  /** 指定された Google Client ID で検証器を構成する。 */
  public GoogleTokenVerifier(@Value("${app.auth.google-client-id}") String clientId) {
    this.verifier =
        new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(List.of(clientId))
            .build();
  }

  /** ID トークンを検証し、ペイロードを返す。検証失敗時は {@link UnauthorizedException} をスローする。 */
  public GoogleIdTokenPayload verify(String idTokenString) {
    GoogleIdToken idToken;
    try {
      idToken = verifier.verify(idTokenString);
    } catch (GeneralSecurityException | IOException e) {
      log.warn("Google ID token verification failed: {}", e.getMessage());
      throw new UnauthorizedException("The Google ID token is invalid.");
    }

    if (idToken == null) {
      throw new UnauthorizedException("The Google ID token is invalid.");
    }

    GoogleIdToken.Payload payload = idToken.getPayload();
    return new GoogleIdTokenPayload(
        payload.getSubject(),
        payload.getEmail(),
        (String) payload.get("name"),
        (String) payload.get("locale"));
  }
}
