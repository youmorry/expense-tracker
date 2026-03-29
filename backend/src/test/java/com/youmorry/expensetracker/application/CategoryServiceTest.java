package com.youmorry.expensetracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.youmorry.expensetracker.domain.category.CategoryType;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class CategoryServiceTest {

  private final CategoryService categoryService = new CategoryService();

  @Test
  void findAll_returnsAllCategoryTypes() {
    var result = categoryService.findAll();

    assertEquals(Arrays.asList(CategoryType.values()), result);
  }
}
