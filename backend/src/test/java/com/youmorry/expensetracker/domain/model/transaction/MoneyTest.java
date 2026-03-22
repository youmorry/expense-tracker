package com.youmorry.expensetracker.domain.model.transaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void constructor_withPositiveValue_createsMoney() {
    assertDoesNotThrow(() -> new Money(new BigDecimal("100")));
  }

  @Test
  void constructor_withNullValue_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new Money(null));
  }

  @Test
  void constructor_withZeroValue_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new Money(BigDecimal.ZERO));
  }

  @Test
  void constructor_withNegativeValue_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new Money(new BigDecimal("-1")));
  }
}
