package com.youmorry.expensetracker.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.youmorry.expensetracker.shared.exception.ResourceNotFoundException;
import com.youmorry.expensetracker.user.domain.User;
import com.youmorry.expensetracker.user.domain.UserId;
import com.youmorry.expensetracker.user.domain.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private UserService userService;

  // --- getMe ---

  @Test
  void getMe_withExistingUser_returnsUser() {
    var userId = new UserId(1L);
    var user =
        new User(
            userId,
            "google-123",
            "test@gmail.com",
            "Test User",
            Instant.parse("2026-01-01T00:00:00Z"));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    User result = userService.getMe(userId);

    assertEquals(user, result);
  }

  @Test
  void getMe_withNonExistingUser_throwsResourceNotFound() {
    var userId = new UserId(999L);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> userService.getMe(userId));
  }

  // --- deleteAccount ---

  @Test
  void deleteAccount_withExistingUser_deletesUser() {
    var userId = new UserId(1L);
    var existingUser =
        new User(
            userId,
            "google-123",
            "test@gmail.com",
            "Test User",
            Instant.parse("2026-01-01T00:00:00Z"));
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

    userService.deleteAccount(userId);

    verify(userRepository).deleteById(userId);
  }

  @Test
  void deleteAccount_withNonExistingUser_throwsResourceNotFound() {
    var userId = new UserId(999L);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> userService.deleteAccount(userId));
  }
}
