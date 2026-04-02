package com.youmorry.expensetracker.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void createNew_withValidArgs_createsUser() {
    var user = User.createNew("google-123", "user@example.com", "Test User");

    assertEquals("google-123", user.googleId());
    assertEquals("user@example.com", user.email());
    assertEquals("Test User", user.displayName());
  }

  @Test
  void constructor_withNullGoogleId_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> new User(new UserId(1L), null, "user@example.com", "Test User", null));
  }

  @Test
  void constructor_withBlankGoogleId_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new User(new UserId(1L), "  ", "user@example.com", "Test User", null));
  }

  @Test
  void constructor_withNullEmail_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> new User(new UserId(1L), "google-123", null, "Test User", null));
  }

  @Test
  void constructor_withBlankEmail_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new User(new UserId(1L), "google-123", "  ", "Test User", null));
  }

  @Test
  void constructor_withNullDisplayName_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> new User(new UserId(1L), "google-123", "user@example.com", null, null));
  }

  @Test
  void constructor_withBlankDisplayName_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new User(new UserId(1L), "google-123", "user@example.com", "  ", null));
  }

  @Test
  void constructor_withDisplayNameExceedingMaxLength_throwsIllegalArgumentException() {
    var longName = "a".repeat(101);

    assertThrows(
        IllegalArgumentException.class,
        () -> new User(new UserId(1L), "google-123", "user@example.com", longName, null));
  }

  @Test
  void constructor_withDisplayNameAtMaxLength_createsUser() {
    var maxName = "a".repeat(100);

    var user = new User(new UserId(1L), "google-123", "user@example.com", maxName, null);

    assertEquals(maxName, user.displayName());
  }
}
