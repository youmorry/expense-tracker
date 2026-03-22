package com.youmorry.expensetracker.domain.model.category;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CategoryIdTest {

  @Test
  void constructor_withPositiveValue_createsCategoryId() {
    assertDoesNotThrow(() -> new CategoryId(1L));
  }

  @Test
  void constructor_withZeroValue_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new CategoryId(0L));
  }

  @Test
  void constructor_withNegativeValue_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new CategoryId(-1L));
  }
}
