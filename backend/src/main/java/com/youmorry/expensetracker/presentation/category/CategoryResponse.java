package com.youmorry.expensetracker.presentation.category;

import com.youmorry.expensetracker.domain.category.CategoryType;

/** カテゴリ単体のレスポンス DTO。 */
public record CategoryResponse(Long id, String name, int displayOrder) {

  /** {@link CategoryType} から {@link CategoryResponse} を生成する。 */
  public static CategoryResponse from(CategoryType categoryType) {
    return new CategoryResponse(
        categoryType.id().value(), categoryType.displayName(), categoryType.displayOrder());
  }
}
