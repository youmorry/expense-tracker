package com.youmorry.expensetracker.domain.transaction;

/**
 * Value Object: Transaction ID。
 *
 * @param value Transaction ID
 */
public record TransactionId(long value) {
  /**
   * ID が正の値であることを検証する。
   *
   * @throws IllegalArgumentException value が 0 以下の場合
   */
  public TransactionId {
    if (value <= 0) {
      throw new IllegalArgumentException("TransactionId value must be positive, but was: " + value);
    }
  }
}
