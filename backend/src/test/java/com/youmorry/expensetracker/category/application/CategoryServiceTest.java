package com.youmorry.expensetracker.category.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.youmorry.expensetracker.category.domain.CategoryType;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CategoryServiceTest {

  private final CategoryService categoryService = new CategoryService();

  @Test
  void findAll_returnsAllCategoryTypes() {
    List<CategoryType> result = categoryService.findAll();

    assertEquals(Arrays.asList(CategoryType.values()), result);
  }
}
