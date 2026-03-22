package com.youmorry.expensetracker.domain.model.category;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CategoryTest {

  @Test
  void constructor_withValidArgs_createsCategory() {
    assertDoesNotThrow(() -> new Category(new CategoryId(1L), "食費", 1));
  }

  @Test
  void constructor_withMaxLengthName_createsCategory() {
    var name = "a".repeat(50);

    assertDoesNotThrow(() -> new Category(new CategoryId(1L), name, 1));
  }

  @Test
  void constructor_withNullName_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new Category(new CategoryId(1L), null, 1));
  }

  @Test
  void constructor_withBlankName_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new Category(new CategoryId(1L), "  ", 1));
  }

  @Test
  void constructor_withEmptyName_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new Category(new CategoryId(1L), "", 1));
  }

  @Test
  void constructor_withNameExceeding50Chars_throwsIllegalArgumentException() {
    var name = "a".repeat(51);

    assertThrows(IllegalArgumentException.class, () -> new Category(new CategoryId(1L), name, 1));
  }

  @Test
  void constructor_withNullDisplayOrder_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new Category(new CategoryId(1L), "食費", null));
  }

  @Test
  void constructor_withZeroDisplayOrder_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new Category(new CategoryId(1L), "食費", 0));
  }

  @Test
  void constructor_withNegativeDisplayOrder_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new Category(new CategoryId(1L), "食費", -1));
  }
}
