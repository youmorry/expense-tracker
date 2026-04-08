package com.youmorry.expensetracker.domain.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserIdTest {

  @Test
  void constructor_withPositiveValue_createsUserId() {
    assertDoesNotThrow(() -> new UserId(1L));
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, -1L})
  void constructor_withNonPositiveValue_throwsIllegalArgumentException(long value) {
    assertThrows(IllegalArgumentException.class, () -> new UserId(value));
  }
}
