package com.youmorry.expensetracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.youmorry.expensetracker.domain.category.Category;
import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.category.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;
  @InjectMocks private CategoryService categoryService;

  @Test
  void findAll_returnsAllCategoriesOrderedByDisplayOrder() {
    var categories =
        List.of(
            new Category(new CategoryId(1L), "Food", 1),
            new Category(new CategoryId(2L), "Transport", 2),
            new Category(new CategoryId(11L), "Uncategorized", 11));
    when(categoryRepository.findAll()).thenReturn(categories);

    var result = categoryService.findAll();

    assertEquals(categories, result);
  }
}
