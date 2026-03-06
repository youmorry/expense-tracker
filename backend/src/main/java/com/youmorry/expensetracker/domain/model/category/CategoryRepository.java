package com.youmorry.expensetracker.domain.model.category;

import java.util.List;
import java.util.Optional;

/** カテゴリの永続化を担うリポジトリインターフェース。 */
public interface CategoryRepository {

  Optional<Category> findById(CategoryId id);

  List<Category> findAll();
}
