package com.youmorry.expensetracker.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youmorry.expensetracker.presentation.HelloController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HelloController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void request_withoutJwt_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/hello")).andExpect(status().isUnauthorized());
  }

  @Test
  void request_withValidJwt_returnsOk() throws Exception {
    mockMvc.perform(get("/api/v1/hello").with(jwt())).andExpect(status().isOk());
  }

  @Test
  void jwtDecoder_withEmptySecret_throwsIllegalArgumentException() {
    SecurityConfig config = new SecurityConfig();
    assertThrows(IllegalArgumentException.class, () -> config.jwtDecoder(""));
  }

  @Test
  void jwtDecoder_withShortSecret_throwsIllegalArgumentException() {
    SecurityConfig config = new SecurityConfig();
    assertThrows(IllegalArgumentException.class, () -> config.jwtDecoder("too-short"));
  }

  @Test
  void jwtDecoder_with32ByteSecret_returnsDecoder() {
    SecurityConfig config = new SecurityConfig();
    assertNotNull(config.jwtDecoder("valid-secret-key-that-is-32-byte!"));
  }
}
