package com.youmorry.expensetracker.domain.model.category;

import java.util.Objects;

/** カテゴリの識別子。 */
public record CategoryId(Long value) {

  public CategoryId {
    Objects.requireNonNull(value, "value must not be null");
    if (value <= 0) {
      throw new IllegalArgumentException("CategoryId value must be positive, but was: " + value);
    }
  }
}
