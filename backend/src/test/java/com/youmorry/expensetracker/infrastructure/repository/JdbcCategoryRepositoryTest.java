package com.youmorry.expensetracker.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.youmorry.expensetracker.domain.model.category.Category;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;

@DataJdbcTest
class JdbcCategoryRepositoryTest extends AbstractRepositoryTest {

  @Autowired private JdbcCategoryRepository categoryRepository;

  @Test
  void findAll_returnsPresetCategories() {
    List<Category> categories = categoryRepository.findAll();

    assertThat(categories).hasSize(11);
    assertThat(categories.getFirst().name()).isEqualTo("Food");
    assertThat(categories.getLast().name()).isEqualTo("Uncategorized");
  }

  @Test
  void findAll_returnsCategoriesOrderedByDisplayOrder() {
    List<Category> categories = categoryRepository.findAll();

    assertThat(categories)
        .extracting(Category::displayOrder)
        .isSorted();
  }
}
