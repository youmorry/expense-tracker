package com.youmorry.expensetracker.domain.model.category;

/** カテゴリの識別子。 */
public record CategoryId(long value) {

  public CategoryId {
    if (value <= 0) {
      throw new IllegalArgumentException("CategoryId value must be positive, but was: " + value);
    }
  }
}
