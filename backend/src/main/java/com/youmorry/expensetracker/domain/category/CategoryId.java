package com.youmorry.expensetracker.domain.category;

/**
 * Value Object: Category ID.
 *
 * @param value Category ID
 */
public record CategoryId(long value) {
  /**
   * ID が正の値であることを検証する。
   *
   * @throws IllegalArgumentException value が 0 以下の場合
   */
  public CategoryId {
    if (value <= 0) {
      throw new IllegalArgumentException("CategoryId value must be positive, but was: " + value);
    }
  }
}
