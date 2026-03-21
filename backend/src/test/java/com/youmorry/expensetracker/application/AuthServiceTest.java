package com.youmorry.expensetracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.youmorry.expensetracker.application.port.JwtTokenGenerator;
import com.youmorry.expensetracker.application.port.OauthTokenVerifier;
import com.youmorry.expensetracker.application.port.OauthUserInfo;
import com.youmorry.expensetracker.domain.model.user.CurrencyCode;
import com.youmorry.expensetracker.domain.model.user.User;
import com.youmorry.expensetracker.domain.model.user.UserId;
import com.youmorry.expensetracker.domain.model.user.UserRepository;
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

  @Mock private OauthTokenVerifier oauthTokenVerifier;
  @Mock private UserRepository userRepository;
  @Mock private JwtTokenGenerator jwtTokenGenerator;
  @InjectMocks private AuthService authService;

  @Test
  void authenticate_withExistingUser_returnsTokenAndUser() {
    var userInfo = new OauthUserInfo("google-123", "test@gmail.com", "Test User", "ja");
    var existingUser =
        new User(
            new UserId(1L),
            "google-123",
            "test@gmail.com",
            "Test User",
            new CurrencyCode("JPY"),
            Instant.parse("2026-01-01T00:00:00Z"));
    when(oauthTokenVerifier.verify("valid-token")).thenReturn(userInfo);
    when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.of(existingUser));
    when(jwtTokenGenerator.generateToken(eq(existingUser.id()), eq(existingUser.email())))
        .thenReturn("jwt-token");

    var result = authService.authenticate("valid-token");

    assertEquals("jwt-token", result.accessToken());
    assertEquals(existingUser, result.user());
    verify(userRepository, never()).save(any());
  }

  @Test
  void authenticate_withNewUser_createsUserAndReturnsToken() {
    var userInfo = new OauthUserInfo("google-new", "new@gmail.com", "New User", "ja");
    var savedUser =
        new User(
            new UserId(2L),
            "google-new",
            "new@gmail.com",
            "New User",
            new CurrencyCode("JPY"),
            Instant.parse("2026-01-01T00:00:00Z"));
    when(oauthTokenVerifier.verify("new-token")).thenReturn(userInfo);
    when(userRepository.findByGoogleId("google-new")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtTokenGenerator.generateToken(eq(savedUser.id()), eq(savedUser.email())))
        .thenReturn("new-jwt-token");

    var result = authService.authenticate("new-token");

    assertEquals("new-jwt-token", result.accessToken());
    assertEquals(savedUser, result.user());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void authenticate_withNewUserAndEnLocale_setsCurrencyToUsd() {
    var userInfo = new OauthUserInfo("google-en", "en@gmail.com", "EN User", "en-US");
    var savedUser =
        new User(
            new UserId(3L),
            "google-en",
            "en@gmail.com",
            "EN User",
            new CurrencyCode("USD"),
            Instant.parse("2026-01-01T00:00:00Z"));
    when(oauthTokenVerifier.verify("en-token")).thenReturn(userInfo);
    when(userRepository.findByGoogleId("google-en")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtTokenGenerator.generateToken(eq(savedUser.id()), eq(savedUser.email())))
        .thenReturn("en-jwt-token");

    var result = authService.authenticate("en-token");

    assertEquals(new CurrencyCode("USD"), result.user().currencyCode());
  }

  @Test
  void authenticate_withInvalidToken_throwsUnauthorized() {
    when(oauthTokenVerifier.verify(anyString()))
        .thenThrow(new UnauthorizedException("The Google ID token is invalid."));

    assertThrows(UnauthorizedException.class, () -> authService.authenticate("invalid-token"));
  }
}
