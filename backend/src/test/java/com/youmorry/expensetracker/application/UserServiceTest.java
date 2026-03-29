package com.youmorry.expensetracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.youmorry.expensetracker.domain.user.User;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.domain.user.UserRepository;
import com.youmorry.expensetracker.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.Currency;
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
            Currency.getInstance("JPY"),
            Instant.parse("2026-01-01T00:00:00Z"));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    var result = userService.getMe(userId);

    assertEquals(user, result);
  }

  @Test
  void getMe_withNonExistingUser_throwsResourceNotFound() {
    var userId = new UserId(999L);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> userService.getMe(userId));
  }

  // --- updateCurrency ---

  @Test
  void updateCurrency_withValidCode_returnsUpdatedUser() {
    var userId = new UserId(1L);
    var existingUser =
        new User(
            userId,
            "google-123",
            "test@gmail.com",
            "Test User",
            Currency.getInstance("JPY"),
            Instant.parse("2026-01-01T00:00:00Z"));
    var updatedUser = existingUser.changeCurrencyCode(Currency.getInstance("USD"));
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(updatedUser)).thenReturn(updatedUser);

    var result = userService.updateCurrency(userId, Currency.getInstance("USD"));

    assertEquals(Currency.getInstance("USD"), result.currencyCode());
    verify(userRepository).save(updatedUser);
  }

  @Test
  void updateCurrency_withSameCode_returnsSameUser() {
    var userId = new UserId(1L);
    var existingUser =
        new User(
            userId,
            "google-123",
            "test@gmail.com",
            "Test User",
            Currency.getInstance("JPY"),
            Instant.parse("2026-01-01T00:00:00Z"));
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(existingUser)).thenReturn(existingUser);

    var result = userService.updateCurrency(userId, Currency.getInstance("JPY"));

    assertEquals(existingUser, result);
    verify(userRepository).save(existingUser);
  }

  @Test
  void updateCurrency_withNonExistingUser_throwsResourceNotFound() {
    var userId = new UserId(999L);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> userService.updateCurrency(userId, Currency.getInstance("USD")));
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
            Currency.getInstance("JPY"),
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
