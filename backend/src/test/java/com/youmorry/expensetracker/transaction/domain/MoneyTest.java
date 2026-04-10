package com.youmorry.expensetracker.transaction.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MoneyTest {

  @ParameterizedTest
  @ValueSource(strings = {"100", "0", "-1"})
  void constructor_withValue_createsMoney(String value) {
    assertDoesNotThrow(() -> new Money(new BigDecimal(value)));
  }

  @Test
  void constructor_withNullValue_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new Money(null));
  }
}
