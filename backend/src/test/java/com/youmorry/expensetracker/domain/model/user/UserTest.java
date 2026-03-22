package com.youmorry.expensetracker.domain.model.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

  @Test
  void createNew_withValidCurrencyCode_createsUser() {
    var user =
        User.createNew("google-123", "user@example.com", "Test User", new CurrencyCode("JPY"));

    assertEquals(new CurrencyCode("JPY"), user.currencyCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"INVALID", "XX", "123", "jp", ""})
  void createNew_withInvalidCurrencyCode_throwsException(String invalidCode) {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            User.createNew(
                "google-123", "user@example.com", "Test User", new CurrencyCode(invalidCode)));
  }

  @Test
  void createNew_withNullCurrencyCode_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> User.createNew("google-123", "user@example.com", "Test User", null));
  }

  @Test
  void changeCurrencyCode_withSameCode_returnsSameInstance() {
    var user = createUser(new CurrencyCode("JPY"));

    var result = user.changeCurrencyCode(new CurrencyCode("JPY"));

    assertSame(user, result);
  }

  @Test
  void changeCurrencyCode_withValidCode_returnsUserWithUpdatedCurrency() {
    var user = createUser(new CurrencyCode("JPY"));

    var updated = user.changeCurrencyCode(new CurrencyCode("USD"));

    assertEquals(new CurrencyCode("USD"), updated.currencyCode());
    assertEquals(user.id(), updated.id());
    assertEquals(user.googleId(), updated.googleId());
    assertEquals(user.email(), updated.email());
    assertEquals(user.displayName(), updated.displayName());
    assertEquals(user.createdAt(), updated.createdAt());
  }

  @ParameterizedTest
  @ValueSource(strings = {"JPY", "USD", "EUR", "GBP", "CNY"})
  void changeCurrencyCode_withVariousValidCodes_returnsUpdatedUser(String currencyCode) {
    var user = createUser(new CurrencyCode("JPY"));

    var updated = user.changeCurrencyCode(new CurrencyCode(currencyCode));

    assertEquals(new CurrencyCode(currencyCode), updated.currencyCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"INVALID", "XX", "123", "jp", ""})
  void changeCurrencyCode_withInvalidCode_throwsException(String invalidCode) {
    var user = createUser(new CurrencyCode("JPY"));

    assertThrows(
        IllegalArgumentException.class,
        () -> user.changeCurrencyCode(new CurrencyCode(invalidCode)));
  }

  @Test
  void changeCurrencyCode_withNull_throwsNullPointerException() {
    var user = createUser(new CurrencyCode("JPY"));

    assertThrows(NullPointerException.class, () -> user.changeCurrencyCode(null));
  }

  private User createUser(CurrencyCode currencyCode) {
    return new User(
        new UserId(1L), "google-123", "user@example.com", "Test User", currencyCode, null);
  }
}
