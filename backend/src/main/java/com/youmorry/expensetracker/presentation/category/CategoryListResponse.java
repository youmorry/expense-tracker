package com.youmorry.expensetracker.presentation.category;

import com.youmorry.expensetracker.domain.category.CategoryType;
import java.util.List;

/** カテゴリ一覧のレスポンス DTO。 */
public record CategoryListResponse(List<CategoryResponse> items) {

  /** {@link CategoryType} のリストから {@link CategoryListResponse} を生成する。 */
  public static CategoryListResponse from(List<CategoryType> categoryTypes) {
    return new CategoryListResponse(
        categoryTypes.stream().map(CategoryResponse::from).toList());
  }
}
