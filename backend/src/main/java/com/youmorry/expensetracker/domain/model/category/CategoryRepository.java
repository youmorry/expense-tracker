package com.youmorry.expensetracker.domain.model.category;

import java.util.List;
import java.util.Optional;

/** カテゴリの永続化を担うリポジトリインターフェース。 */
public interface CategoryRepository {

  /** 指定された ID のカテゴリを取得する。 */
  Optional<Category> findById(CategoryId id);

  /** カテゴリを全件取得する。 */
  List<Category> findAll();
}
