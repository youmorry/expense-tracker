package com.youmorry.expensetracker.presentation.category;

import com.youmorry.expensetracker.domain.model.category.Category;

/** カテゴリ単体のレスポンス DTO。 */
public record CategoryResponse(Long id, String name, int displayOrder) {

  /** {@link Category} から {@link CategoryResponse} を生成する。 */
  public static CategoryResponse from(Category category) {
    return new CategoryResponse(category.id().value(), category.name(), category.displayOrder());
  }
}
