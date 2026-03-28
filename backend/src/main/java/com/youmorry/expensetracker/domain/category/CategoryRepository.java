package com.youmorry.expensetracker.domain.category;

import java.util.List;
import java.util.Optional;

/** カテゴリの永続化を担うリポジトリインターフェース。 */
public interface CategoryRepository {

  /**
   * 指定された ID のカテゴリを取得する。
   *
   * @param id カテゴリ ID
   * @return カテゴリ。存在しない場合は空
   */
  Optional<Category> findById(CategoryId id);

  /**
   * カテゴリを全件取得する。
   *
   * @return カテゴリのリスト
   */
  List<Category> findAll();
}
