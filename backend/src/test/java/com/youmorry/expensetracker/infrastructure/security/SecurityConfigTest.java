package com.youmorry.expensetracker.infrastructure.security;

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
}
