package com.youmorry.expensetracker.presentation.category;

import com.youmorry.expensetracker.domain.category.Category;
import java.util.List;

/** カテゴリ一覧のレスポンス DTO。 */
public record CategoryListResponse(List<CategoryResponse> items) {

  /** {@link Category} のリストから {@link CategoryListResponse} を生成する。 */
  public static CategoryListResponse from(List<Category> categories) {
    return new CategoryListResponse(categories.stream().map(CategoryResponse::from).toList());
  }
}
