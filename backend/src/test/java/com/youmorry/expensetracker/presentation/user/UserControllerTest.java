package com.youmorry.expensetracker.presentation.user;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youmorry.expensetracker.application.UserService;
import com.youmorry.expensetracker.domain.user.CurrencyCode;
import com.youmorry.expensetracker.domain.user.User;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.infrastructure.security.SecurityConfig;
import com.youmorry.expensetracker.infrastructure.web.WebMvcConfig;
import com.youmorry.expensetracker.shared.exception.ResourceNotFoundException;
import com.youmorry.expensetracker.shared.exception.ValidationException;
import com.youmorry.expensetracker.shared.exception.ValidationException.FieldError;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, WebMvcConfig.class})
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserService userService;

  // --- GET /api/v1/users/me ---

  @Test
  void getMe_withoutJwt_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void getMe_withValidJwt_returnsOkWithUser() throws Exception {
    var userId = new UserId(1L);
    var user =
        new User(
            userId,
            "google-123",
            "test@gmail.com",
            "Test User",
            new CurrencyCode("JPY"),
            Instant.parse("2026-01-01T00:00:00Z"));
    when(userService.getMe(userId)).thenReturn(user);

    mockMvc
        .perform(get("/api/v1/users/me").with(jwt().jwt(j -> j.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("test@gmail.com"))
        .andExpect(jsonPath("$.display_name").value("Test User"))
        .andExpect(jsonPath("$.currency_code").value("JPY"))
        .andExpect(jsonPath("$.created_at").value("2026-01-01T00:00:00Z"));
  }

  @Test
  void getMe_withNonExistentUser_returns404() throws Exception {
    when(userService.getMe(new UserId(999L)))
        .thenThrow(new ResourceNotFoundException("User not found."));

    mockMvc
        .perform(get("/api/v1/users/me").with(jwt().jwt(j -> j.subject("999"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  // --- PATCH /api/v1/users/me/currency ---

  @Test
  void updateCurrency_withoutJwt_returns401() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/users/me/currency")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currency_code\": \"USD\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void updateCurrency_withValidRequest_returnsOkWithUpdatedUser() throws Exception {
    var userId = new UserId(1L);
    var updatedUser =
        new User(
            userId,
            "google-123",
            "test@gmail.com",
            "Test User",
            new CurrencyCode("USD"),
            Instant.parse("2026-01-01T00:00:00Z"));
    when(userService.updateCurrency(userId, "USD")).thenReturn(updatedUser);

    mockMvc
        .perform(
            patch("/api/v1/users/me/currency")
                .with(jwt().jwt(j -> j.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currency_code\": \"USD\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.currency_code").value("USD"));

    verify(userService).updateCurrency(userId, "USD");
  }

  @Test
  void updateCurrency_withInvalidCurrencyCode_returns422() throws Exception {
    when(userService.updateCurrency(new UserId(1L), "INVALID"))
        .thenThrow(
            new ValidationException(
                "Invalid ISO 4217 currency code: INVALID",
                List.of(new FieldError("Invalid ISO 4217 currency code.", "currencyCode"))));

    mockMvc
        .perform(
            patch("/api/v1/users/me/currency")
                .with(jwt().jwt(j -> j.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currency_code\": \"INVALID\"}"))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.type").value("/errors/validation-error"))
        .andExpect(jsonPath("$.errors[0].pointer").value("#/currency_code"));

    verify(userService).updateCurrency(new UserId(1L), "INVALID");
  }

  @Test
  void updateCurrency_withMissingCurrencyCode_returns422() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/users/me/currency")
                .with(jwt().jwt(j -> j.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.errors[0].pointer").value("#/currency_code"))
        .andExpect(jsonPath("$.errors[0].detail").value("must not be blank"));

    verify(userService, never()).updateCurrency(eq(new UserId(1L)), anyString());
  }

  // --- DELETE /api/v1/users/me ---

  @Test
  void deleteAccount_withoutJwt_returns401() throws Exception {
    mockMvc.perform(delete("/api/v1/users/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void deleteAccount_withValidJwt_returns204() throws Exception {
    mockMvc
        .perform(delete("/api/v1/users/me").with(jwt().jwt(j -> j.subject("1"))))
        .andExpect(status().isNoContent());

    verify(userService).deleteAccount(new UserId(1L));
  }

  @Test
  void deleteAccount_withNonExistentUser_returns404() throws Exception {
    var userId = new UserId(999L);
    org.mockito.Mockito.doThrow(new ResourceNotFoundException("User not found."))
        .when(userService)
        .deleteAccount(userId);

    mockMvc
        .perform(delete("/api/v1/users/me").with(jwt().jwt(j -> j.subject("999"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }
}
