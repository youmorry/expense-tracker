package com.youmorry.expensetracker.domain.model.transaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TransactionIdTest {

  @Test
  void constructor_withPositiveValue_createsTransactionId() {
    assertDoesNotThrow(() -> new TransactionId(1L));
  }

  @Test
  void constructor_withNullValue_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new TransactionId(null));
  }

  @Test
  void constructor_withZeroValue_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new TransactionId(0L));
  }

  @Test
  void constructor_withNegativeValue_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new TransactionId(-1L));
  }
}
