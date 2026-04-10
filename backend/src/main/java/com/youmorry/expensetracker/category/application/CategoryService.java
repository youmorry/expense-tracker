package com.youmorry.expensetracker.category.application;

import com.youmorry.expensetracker.category.domain.CategoryType;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

/** カテゴリ取得のユースケースを実装する。 */
@Service
public class CategoryService {

  /**
   * カテゴリを displayOrder 昇順で全件取得する。
   *
   * @return カテゴリのリスト
   */
  public List<CategoryType> findAll() {
    return Arrays.asList(CategoryType.values());
  }
}
