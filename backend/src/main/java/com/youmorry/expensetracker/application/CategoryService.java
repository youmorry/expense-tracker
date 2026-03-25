package com.youmorry.expensetracker.application;

import com.youmorry.expensetracker.domain.category.Category;
import com.youmorry.expensetracker.domain.category.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/** カテゴリ取得のユースケースを実装する。 */
@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;

  /**
   * コンストラクタ。
   *
   * @param categoryRepository カテゴリリポジトリ
   */
  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  /**
   * カテゴリを display_order 昇順で全件取得する。
   *
   * @return カテゴリのリスト
   */
  public List<Category> findAll() {
    return categoryRepository.findAll();
  }
}
