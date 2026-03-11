package com.youmorry.expensetracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.youmorry.expensetracker.domain.model.user.User;
import com.youmorry.expensetracker.domain.model.user.UserId;
import com.youmorry.expensetracker.domain.model.user.UserRepository;
import com.youmorry.expensetracker.infrastructure.security.GoogleIdTokenPayload;
import com.youmorry.expensetracker.infrastructure.security.GoogleTokenVerifier;
import com.youmorry.expensetracker.infrastructure.security.JwtProvider;
import com.youmorry.expensetracker.shared.exception.UnauthorizedException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private GoogleTokenVerifier googleTokenVerifier;
  @Mock private UserRepository userRepository;
  @Mock private JwtProvider jwtProvider;
  @InjectMocks private AuthService authService;

  @Test
  void authenticate_withExistingUser_returnsTokenAndUser() {
    var payload = new GoogleIdTokenPayload("google-123", "test@gmail.com", "Test User", "ja");
    var existingUser =
        new User(
            new UserId(1L),
            "google-123",
            "test@gmail.com",
            "Test User",
            "JPY",
            Instant.parse("2026-01-01T00:00:00Z"));
    when(googleTokenVerifier.verify("valid-token")).thenReturn(payload);
    when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.of(existingUser));
    when(jwtProvider.generateToken(existingUser)).thenReturn("jwt-token");

    var result = authService.authenticate("valid-token");

    assertEquals("jwt-token", result.accessToken());
    assertEquals(existingUser, result.user());
    verify(userRepository, never()).save(any());
  }

  @Test
  void authenticate_withNewUser_createsUserAndReturnsToken() {
    var payload = new GoogleIdTokenPayload("google-new", "new@gmail.com", "New User", "ja");
    var savedUser =
        new User(
            new UserId(2L),
            "google-new",
            "new@gmail.com",
            "New User",
            "JPY",
            Instant.parse("2026-01-01T00:00:00Z"));
    when(googleTokenVerifier.verify("new-token")).thenReturn(payload);
    when(userRepository.findByGoogleId("google-new")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtProvider.generateToken(savedUser)).thenReturn("new-jwt-token");

    var result = authService.authenticate("new-token");

    assertEquals("new-jwt-token", result.accessToken());
    assertEquals(savedUser, result.user());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void authenticate_withNewUserAndEnLocale_setsCurrencyToUsd() {
    var payload = new GoogleIdTokenPayload("google-en", "en@gmail.com", "EN User", "en-US");
    var savedUser =
        new User(
            new UserId(3L),
            "google-en",
            "en@gmail.com",
            "EN User",
            "USD",
            Instant.parse("2026-01-01T00:00:00Z"));
    when(googleTokenVerifier.verify("en-token")).thenReturn(payload);
    when(userRepository.findByGoogleId("google-en")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtProvider.generateToken(savedUser)).thenReturn("en-jwt-token");

    var result = authService.authenticate("en-token");

    assertEquals("USD", result.user().currencyCode());
  }

  @Test
  void authenticate_withInvalidToken_throwsUnauthorized() {
    when(googleTokenVerifier.verify(anyString()))
        .thenThrow(new UnauthorizedException("The Google ID token is invalid."));

    assertThrows(UnauthorizedException.class, () -> authService.authenticate("invalid-token"));
  }
}
