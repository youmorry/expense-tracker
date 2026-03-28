package com.youmorry.expensetracker.domain.category;

/**
 * Value Object: Category ID
 *
 * @param value Category ID
 */
public record CategoryId(long value) {
  /** constructor */
  public CategoryId {
    if (value <= 0) {
      throw new IllegalArgumentException("CategoryId value must be positive, but was: " + value);
    }
  }
}
