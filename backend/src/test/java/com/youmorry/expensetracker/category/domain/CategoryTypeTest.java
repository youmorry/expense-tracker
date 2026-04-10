package com.youmorry.expensetracker.category.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CategoryTypeTest {

  @Test
  void values_allHaveUniqueIds() {
    Set<CategoryId> ids =
        Arrays.stream(CategoryType.values()).map(CategoryType::id).collect(Collectors.toSet());

    assertEquals(CategoryType.values().length, ids.size());
  }

  @Test
  void values_allHaveUniqueDisplayOrders() {
    Set<Integer> orders =
        Arrays.stream(CategoryType.values())
            .map(CategoryType::displayOrder)
            .collect(Collectors.toSet());

    assertEquals(CategoryType.values().length, orders.size());
  }

  @Test
  void fromId_withValidId_returnsCorrectType() {
    assertEquals(CategoryType.FOOD, CategoryType.fromId(new CategoryId(1L)));
    assertEquals(CategoryType.UNCATEGORIZED, CategoryType.fromId(new CategoryId(11L)));
  }

  @Test
  void fromId_withInvalidId_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> CategoryType.fromId(new CategoryId(999L)));
  }

  @Test
  void findById_withValidId_returnsOptionalContainingType() {
    Optional<CategoryType> result = CategoryType.findById(new CategoryId(1L));

    assertTrue(result.isPresent());
    assertEquals(CategoryType.FOOD, result.get());
  }

  @Test
  void findById_withInvalidId_returnsEmpty() {
    Optional<CategoryType> result = CategoryType.findById(new CategoryId(999L));

    assertTrue(result.isEmpty());
  }
}
