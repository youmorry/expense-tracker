package com.youmorry.expensetracker.category.presentation;

import com.youmorry.expensetracker.category.domain.CategoryType;
import java.util.List;

/** カテゴリ一覧のレスポンス DTO。 */
public record CategoryListResponse(List<CategoryResponse> items) {

  /** {@link CategoryType} のリストから {@link CategoryListResponse} を生成する。 */
  public static CategoryListResponse from(List<CategoryType> categoryTypes) {
    return new CategoryListResponse(categoryTypes.stream().map(CategoryResponse::from).toList());
  }
}
