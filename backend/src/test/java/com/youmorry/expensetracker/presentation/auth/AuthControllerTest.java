package com.youmorry.expensetracker.presentation.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youmorry.expensetracker.application.AuthService;
import com.youmorry.expensetracker.application.AuthService.AuthResult;
import com.youmorry.expensetracker.domain.user.User;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.shared.exception.UnauthorizedException;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AuthService authService;

  @Test
  void authenticateWithGoogle_withValidToken_returnsOkWithTokenAndUser() throws Exception {
    var user =
        new User(
            new UserId(1L),
            "google-123",
            "test@gmail.com",
            "Test User",
            Currency.getInstance("JPY"),
            Instant.parse("2026-01-01T00:00:00Z"));
    var authResult = new AuthResult("jwt-token-value", user);
    when(authService.authenticate(eq("valid-id-token"), eq(Locale.forLanguageTag("ja-JP"))))
        .thenReturn(authResult);

    mockMvc
        .perform(
            post("/api/v1/auth/google")
                .locale(Locale.forLanguageTag("ja-JP"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_token\": \"valid-id-token\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").value("jwt-token-value"))
        .andExpect(jsonPath("$.user.id").value(1))
        .andExpect(jsonPath("$.user.email").value("test@gmail.com"))
        .andExpect(jsonPath("$.user.display_name").value("Test User"))
        .andExpect(jsonPath("$.user.currency_code").value("JPY"))
        .andExpect(jsonPath("$.user.created_at").value("2026-01-01T00:00:00Z"));
  }

  @Test
  void authenticateWithGoogle_withoutAcceptLanguageHeader_usesDefaultLocale() throws Exception {
    var user =
        new User(
            new UserId(1L),
            "google-123",
            "test@gmail.com",
            "Test User",
            Currency.getInstance("USD"),
            Instant.parse("2026-01-01T00:00:00Z"));
    var authResult = new AuthResult("jwt-token-value", user);
    when(authService.authenticate(eq("valid-id-token"), eq(Locale.ENGLISH))).thenReturn(authResult);

    mockMvc
        .perform(
            post("/api/v1/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_token\": \"valid-id-token\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.currency_code").value("USD"));
  }

  @Test
  void authenticateWithGoogle_withInvalidToken_returns401() throws Exception {
    when(authService.authenticate(anyString(), eq(Locale.forLanguageTag("ja-JP"))))
        .thenThrow(new UnauthorizedException("The Google ID token is invalid."));

    mockMvc
        .perform(
            post("/api/v1/auth/google")
                .locale(Locale.forLanguageTag("ja-JP"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_token\": \"invalid-token\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.type").value("/errors/unauthorized"))
        .andExpect(jsonPath("$.status").value(401));
  }

  @Test
  void authenticateWithGoogle_withMissingIdToken_returns422() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/google").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.errors[0].pointer").value("#/id_token"))
        .andExpect(jsonPath("$.errors[0].detail").value("must not be blank"));
  }

  @Test
  void authenticateWithGoogle_withEmptyBody_returns400() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/google").contentType(MediaType.APPLICATION_JSON).content(""))
        .andExpect(status().isBadRequest());
  }
}
