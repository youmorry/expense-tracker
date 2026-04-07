package com.youmorry.expensetracker.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.youmorry.expensetracker.domain.user.User;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.domain.user.UserRepository;
import com.youmorry.expensetracker.shared.exception.UnauthorizedException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
    var userInfo = new OauthUserInfo("google-123", "test@gmail.com", "Test User");
    var existingUser =
        new User(
            new UserId(1L),
            "google-123",
            "test@gmail.com",
            "Test User",
            Instant.parse("2026-01-01T00:00:00Z"));
    when(oauthTokenVerifier.verify("valid-token")).thenReturn(userInfo);
    when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.of(existingUser));
    when(jwtTokenGenerator.generateToken(eq(existingUser.id()), eq(existingUser.email())))
        .thenReturn("jwt-token");

    AuthService.AuthResult result = authService.authenticate("valid-token");

    assertEquals("jwt-token", result.accessToken());
    assertEquals(existingUser, result.user());
    verify(userRepository, never()).save(any());
  }

  @Test
  void authenticate_withNewUser_returnsTokenAndCreatedUser() {
    var userInfo = new OauthUserInfo("google-new", "new@gmail.com", "New User");
    var savedUser =
        new User(
            new UserId(2L),
            "google-new",
            "new@gmail.com",
            "New User",
            Instant.parse("2026-01-01T00:00:00Z"));
    when(oauthTokenVerifier.verify("new-token")).thenReturn(userInfo);
    when(userRepository.findByGoogleId("google-new")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtTokenGenerator.generateToken(eq(savedUser.id()), eq(savedUser.email())))
        .thenReturn("new-jwt-token");

    AuthService.AuthResult result = authService.authenticate("new-token");

    assertEquals("new-jwt-token", result.accessToken());
    assertEquals(savedUser, result.user());
    verify(userRepository).save(any(User.class));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  void authenticate_withNewUserAndMissingName_returnsTokenWithDefaultDisplayName(String name) {
    var userInfo = new OauthUserInfo("google-noname", "noname@gmail.com", name);
    var savedUser =
        new User(
            new UserId(3L),
            "google-noname",
            "noname@gmail.com",
            "USER",
            Instant.parse("2026-01-01T00:00:00Z"));
    when(oauthTokenVerifier.verify("noname-token")).thenReturn(userInfo);
    when(userRepository.findByGoogleId("google-noname")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtTokenGenerator.generateToken(eq(savedUser.id()), eq(savedUser.email())))
        .thenReturn("noname-jwt-token");

    AuthService.AuthResult result = authService.authenticate("noname-token");

    assertEquals("noname-jwt-token", result.accessToken());
    assertEquals("USER", result.user().displayName());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void authenticate_withInvalidToken_throwsUnauthorized() {
    when(oauthTokenVerifier.verify(anyString()))
        .thenThrow(new UnauthorizedException("The Google ID token is invalid."));

    assertThrows(UnauthorizedException.class, () -> authService.authenticate("invalid-token"));
  }
}
