package com.youmorry.expensetracker.domain.model.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

  @Test
  void changeCurrencyCode_withValidCode_returnsUserWithUpdatedCurrency() {
    var user = createUser("JPY");

    var updated = user.changeCurrencyCode("USD");

    assertEquals("USD", updated.currencyCode());
    assertEquals(user.id(), updated.id());
    assertEquals(user.googleId(), updated.googleId());
    assertEquals(user.email(), updated.email());
    assertEquals(user.displayName(), updated.displayName());
    assertEquals(user.createdAt(), updated.createdAt());
  }

  @ParameterizedTest
  @ValueSource(strings = {"JPY", "USD", "EUR", "GBP", "CNY"})
  void changeCurrencyCode_withVariousValidCodes_returnsUpdatedUser(String currencyCode) {
    var user = createUser("JPY");

    var updated = user.changeCurrencyCode(currencyCode);

    assertEquals(currencyCode, updated.currencyCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"INVALID", "XX", "123", "jp", ""})
  void changeCurrencyCode_withInvalidCode_throwsException(String invalidCode) {
    var user = createUser("JPY");

    assertThrows(IllegalArgumentException.class, () -> user.changeCurrencyCode(invalidCode));
  }

  @Test
  void changeCurrencyCode_withNull_throwsNullPointerException() {
    var user = createUser("JPY");

    assertThrows(NullPointerException.class, () -> user.changeCurrencyCode(null));
  }

  private User createUser(String currencyCode) {
    return new User(
        new UserId(1L), "google-123", "user@example.com", "Test User", currencyCode, null);
  }
}
