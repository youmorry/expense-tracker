package com.youmorry.expensetracker.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void createNew_withValidCurrencyCode_createsUser() {
    var user = User.createNew("google-123", "user@example.com", "Test User", CurrencyCode.JPY);

    assertEquals(CurrencyCode.JPY, user.currencyCode());
  }

  @Test
  void createNew_withNullCurrencyCode_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> User.createNew("google-123", "user@example.com", "Test User", null));
  }

  @Test
  void constructor_withNullGoogleId_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new User(
                new UserId(1L),
                null,
                "user@example.com",
                "Test User",
                CurrencyCode.JPY,
                null));
  }

  @Test
  void constructor_withBlankGoogleId_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new User(
                new UserId(1L),
                "  ",
                "user@example.com",
                "Test User",
                CurrencyCode.JPY,
                null));
  }

  @Test
  void constructor_withNullEmail_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new User(
                new UserId(1L), "google-123", null, "Test User", CurrencyCode.JPY, null));
  }

  @Test
  void constructor_withBlankEmail_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new User(
                new UserId(1L), "google-123", "  ", "Test User", CurrencyCode.JPY, null));
  }

  @Test
  void constructor_withNullDisplayName_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new User(
                new UserId(1L),
                "google-123",
                "user@example.com",
                null,
                CurrencyCode.JPY,
                null));
  }

  @Test
  void constructor_withBlankDisplayName_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new User(
                new UserId(1L),
                "google-123",
                "user@example.com",
                "  ",
                CurrencyCode.JPY,
                null));
  }

  @Test
  void constructor_withDisplayNameExceedingMaxLength_throwsIllegalArgumentException() {
    var longName = "a".repeat(101);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new User(
                new UserId(1L),
                "google-123",
                "user@example.com",
                longName,
                CurrencyCode.JPY,
                null));
  }

  @Test
  void constructor_withDisplayNameAtMaxLength_createsUser() {
    var maxName = "a".repeat(100);

    var user =
        new User(
            new UserId(1L),
            "google-123",
            "user@example.com",
            maxName,
            CurrencyCode.JPY,
            null);

    assertEquals(maxName, user.displayName());
  }

  @Test
  void constructor_withNullCurrencyCode_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> new User(new UserId(1L), "google-123", "user@example.com", "Test User", null, null));
  }

  @Test
  void changeCurrencyCode_withSameCode_returnsSameInstance() {
    var user = createUser(CurrencyCode.JPY);

    var result = user.changeCurrencyCode(CurrencyCode.JPY);

    assertSame(user, result);
  }

  @Test
  void changeCurrencyCode_withValidCode_returnsUserWithUpdatedCurrency() {
    var user = createUser(CurrencyCode.JPY);

    var updated = user.changeCurrencyCode(CurrencyCode.USD);

    assertEquals(CurrencyCode.USD, updated.currencyCode());
    assertEquals(user.id(), updated.id());
    assertEquals(user.googleId(), updated.googleId());
    assertEquals(user.email(), updated.email());
    assertEquals(user.displayName(), updated.displayName());
    assertEquals(user.createdAt(), updated.createdAt());
  }

  @Test
  void changeCurrencyCode_withNull_throwsNullPointerException() {
    var user = createUser(CurrencyCode.JPY);

    assertThrows(NullPointerException.class, () -> user.changeCurrencyCode(null));
  }

  private User createUser(CurrencyCode currencyCode) {
    return new User(
        new UserId(1L), "google-123", "user@example.com", "Test User", currencyCode, null);
  }
}
