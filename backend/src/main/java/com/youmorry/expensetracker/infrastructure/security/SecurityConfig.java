package com.youmorry.expensetracker.infrastructure.security;

import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/** Spring Security の設定。 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /** セキュリティフィルターチェーンを構成する。 */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/v1/auth/**").permitAll().anyRequest().authenticated());
    return http.build();
  }

  /** HS256 で署名された JWT を検証する {@link JwtDecoder} を構成する。issuer の一致も検証する。 */
  @Bean
  public JwtDecoder jwtDecoder(
      @Value("${app.auth.jwt-secret}") String secret,
      @Value("${app.auth.jwt-issuer}") String issuer) {
    byte[] keyBytes;
    try {
      keyBytes = Base64.getDecoder().decode(secret);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "app.auth.jwt-secret must be a valid Base64-encoded string", e);
    }
    if (keyBytes.length < 32) {
      throw new IllegalArgumentException(
          "app.auth.jwt-secret must be at least 32 bytes for HS256, but was "
              + keyBytes.length
              + " bytes");
    }
    SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
    return decoder;
  }
}
