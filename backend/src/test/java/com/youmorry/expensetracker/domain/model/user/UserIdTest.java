package com.youmorry.expensetracker.domain.model.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserIdTest {

  @Test
  void constructor_withPositiveValue_createsUserId() {
    assertDoesNotThrow(() -> new UserId(1L));
  }

  @Test
  void constructor_withZeroValue_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new UserId(0L));
  }

  @Test
  void constructor_withNegativeValue_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new UserId(-1L));
  }
}
