package com.youmorry.expensetracker.user.presentation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youmorry.expensetracker.user.application.UserService;
import com.youmorry.expensetracker.user.domain.User;
import com.youmorry.expensetracker.user.domain.UserId;
import com.youmorry.expensetracker.auth.infrastructure.SecurityConfig;
import com.youmorry.expensetracker.shared.infrastructure.web.WebMvcConfig;
import com.youmorry.expensetracker.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
            Instant.parse("2026-01-01T00:00:00Z"));
    when(userService.getMe(userId)).thenReturn(user);

    mockMvc
        .perform(get("/api/v1/users/me").with(jwt().jwt(j -> j.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("test@gmail.com"))
        .andExpect(jsonPath("$.display_name").value("Test User"))
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
