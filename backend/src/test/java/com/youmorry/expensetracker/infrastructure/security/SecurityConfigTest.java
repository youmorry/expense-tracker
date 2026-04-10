package com.youmorry.expensetracker.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youmorry.expensetracker.domain.user.UserId;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SecurityTestController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

  // 32 bytes of key material, Base64-encoded
  private static final String SECRET =
      Base64.getEncoder()
          .encodeToString("test-secret-key-at-least-32-byte".getBytes(StandardCharsets.UTF_8));
  private static final String ISSUER = "https://test.example.com";

  @Autowired private MockMvc mockMvc;

  @Test
  void request_withoutJwt_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/test")).andExpect(status().isUnauthorized());
  }

  @Test
  void request_withValidJwt_returnsOk() throws Exception {
    mockMvc.perform(get("/api/v1/test").with(jwt())).andExpect(status().isOk());
  }

  @Test
  void jwtDecoder_withEmptySecret_throwsIllegalArgumentException() {
    SecurityConfig config = new SecurityConfig();
    assertThrows(IllegalArgumentException.class, () -> config.jwtDecoder("", ISSUER));
  }

  @Test
  void jwtDecoder_withInvalidBase64_throwsIllegalArgumentException() {
    SecurityConfig config = new SecurityConfig();
    assertThrows(
        IllegalArgumentException.class, () -> config.jwtDecoder("not-valid-base64!", ISSUER));
  }

  @Test
  void jwtDecoder_withShortSecret_throwsIllegalArgumentException() {
    SecurityConfig config = new SecurityConfig();
    // Base64 of "short" (only 5 bytes, less than 32)
    String shortBase64 =
        Base64.getEncoder().encodeToString("short".getBytes(StandardCharsets.UTF_8));
    assertThrows(IllegalArgumentException.class, () -> config.jwtDecoder(shortBase64, ISSUER));
  }

  @Test
  void jwtDecoder_with32ByteSecret_returnsDecoder() {
    SecurityConfig config = new SecurityConfig();
    String validBase64 =
        Base64.getEncoder()
            .encodeToString("valid-secret-key-that-is-32-byte".getBytes(StandardCharsets.UTF_8));
    assertNotNull(config.jwtDecoder(validBase64, ISSUER));
  }

  @Test
  void jwtDecoder_withWrongIssuerToken_throwsJwtValidationException() {
    SecurityConfig config = new SecurityConfig();
    JwtDecoder decoder = config.jwtDecoder(SECRET, ISSUER);

    JwtProvider provider = new JwtProvider(SECRET, "https://wrong-issuer.example.com", 24);
    String token = provider.generateToken(new UserId(1L), "test@gmail.com");

    assertThrows(JwtValidationException.class, () -> decoder.decode(token));
  }

  @Test
  void jwtDecoder_withCorrectIssuerToken_returnsDecodedJwt() {
    SecurityConfig config = new SecurityConfig();
    JwtDecoder decoder = config.jwtDecoder(SECRET, ISSUER);

    JwtProvider provider = new JwtProvider(SECRET, ISSUER, 24);
    String token = provider.generateToken(new UserId(1L), "test@gmail.com");

    assertNotNull(decoder.decode(token));
  }
}
