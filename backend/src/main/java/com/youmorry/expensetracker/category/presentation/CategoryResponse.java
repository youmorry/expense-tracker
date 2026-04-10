package com.youmorry.expensetracker.category.presentation;

import com.youmorry.expensetracker.category.domain.CategoryType;

/** カテゴリ単体のレスポンス DTO。 */
public record CategoryResponse(Long id, String name, int displayOrder) {

  /** {@link CategoryType} から {@link CategoryResponse} を生成する。 */
  public static CategoryResponse from(CategoryType categoryType) {
    return new CategoryResponse(
        categoryType.id().value(), categoryType.displayName(), categoryType.displayOrder());
  }
}
