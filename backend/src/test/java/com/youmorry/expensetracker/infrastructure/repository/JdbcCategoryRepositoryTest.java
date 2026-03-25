package com.youmorry.expensetracker.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.youmorry.expensetracker.domain.category.Category;
import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.category.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;

@DataJdbcTest
class JdbcCategoryRepositoryTest extends AbstractRepositoryTest {

  @Autowired private CategoryRepository categoryRepository;

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

    assertThat(categories).extracting(Category::displayOrder).isSorted();
  }

  @Test
  void findById_existingId_returnsCategory() {
    Optional<Category> category = categoryRepository.findById(new CategoryId(1));

    assertThat(category).isPresent();
    assertThat(category.get().name()).isEqualTo("Food");
  }

  @Test
  void findById_nonExistingId_returnsEmpty() {
    Optional<Category> category = categoryRepository.findById(new CategoryId(9999));

    assertThat(category).isEmpty();
  }
}
