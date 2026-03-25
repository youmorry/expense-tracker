package com.youmorry.expensetracker.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CurrencyCodeTest {

  @ParameterizedTest
  @ValueSource(strings = {"JPY", "USD", "EUR", "GBP", "CNY"})
  void constructor_withValidCode_createsCurrencyCode(String code) {
    var currencyCode = new CurrencyCode(code);

    assertEquals(code, currencyCode.value());
  }

  @Test
  void constructor_withNull_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new CurrencyCode(null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " "})
  void constructor_withBlankCode_throwsIllegalArgumentException(String code) {
    assertThrows(IllegalArgumentException.class, () -> new CurrencyCode(code));
  }

  @ParameterizedTest
  @ValueSource(strings = {"INVALID", "XX", "123", "jp"})
  void constructor_withNonIso4217Code_throwsIllegalArgumentException(String code) {
    assertThrows(IllegalArgumentException.class, () -> new CurrencyCode(code));
  }
}
