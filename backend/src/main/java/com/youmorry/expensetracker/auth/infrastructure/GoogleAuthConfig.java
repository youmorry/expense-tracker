package com.youmorry.expensetracker.auth.infrastructure;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Google 認証に関連する Bean 定義。 */
@Configuration
@Profile("!local")
public class GoogleAuthConfig {

  /**
   * Google ID トークン検証器を構成する。
   *
   * @param clientId Google OAuth2 クライアント ID
   * @return 構成済みの {@link GoogleIdTokenVerifier}
   */
  @Bean
  GoogleIdTokenVerifier googleIdTokenVerifier(
      @Value("${app.auth.google-client-id}") String clientId) {
    return new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(), GsonFactory.getDefaultInstance())
        .setAudience(List.of(clientId))
        .build();
  }
}
