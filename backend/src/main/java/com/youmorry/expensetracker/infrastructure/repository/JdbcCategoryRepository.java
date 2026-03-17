package com.youmorry.expensetracker.infrastructure.repository;

import com.youmorry.expensetracker.domain.model.category.Category;
import com.youmorry.expensetracker.domain.model.category.CategoryId;
import com.youmorry.expensetracker.domain.model.category.CategoryRepository;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JDBC による {@link CategoryRepository} の実装。 */
@Repository
public interface JdbcCategoryRepository
    extends CategoryRepository, CrudRepository<Category, CategoryId> {

  @Override
  @Query("SELECT * FROM categories ORDER BY display_order ASC")
  List<Category> findAll();
}
