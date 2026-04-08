package com.youmorry.expensetracker.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UserTest {

  @Test
  void createNew_withValidArgs_createsUser() {
    var user = User.createNew("google-123", "user@example.com", "Test User");

    assertEquals("google-123", user.googleId());
    assertEquals("user@example.com", user.email());
    assertEquals("Test User", user.displayName());
  }

  @ParameterizedTest
  @MethodSource("nullFieldArgs")
  void constructor_withNullRequiredField_throwsNullPointerException(
      String googleId, String email, String displayName) {
    assertThrows(
        NullPointerException.class,
        () -> new User(new UserId(1L), googleId, email, displayName, null));
  }

  static Stream<Arguments> nullFieldArgs() {
    return Stream.of(
        Arguments.of(null, "user@example.com", "Test User"),
        Arguments.of("google-123", null, "Test User"),
        Arguments.of("google-123", "user@example.com", null));
  }

  @ParameterizedTest
  @MethodSource("blankFieldArgs")
  void constructor_withBlankRequiredField_throwsIllegalArgumentException(
      String googleId, String email, String displayName) {
    assertThrows(
        IllegalArgumentException.class,
        () -> new User(new UserId(1L), googleId, email, displayName, null));
  }

  static Stream<Arguments> blankFieldArgs() {
    return Stream.of(
        Arguments.of("  ", "user@example.com", "Test User"),
        Arguments.of("google-123", "  ", "Test User"),
        Arguments.of("google-123", "user@example.com", "  "));
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
