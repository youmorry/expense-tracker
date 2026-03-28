package com.youmorry.expensetracker.domain.transaction;

/**
 * Value Object: Transaction ID
 *
 * @param value Transaction ID
 */
public record TransactionId(long value) {
  /** constructor */
  public TransactionId {
    if (value <= 0) {
      throw new IllegalArgumentException("TransactionId value must be positive, but was: " + value);
    }
  }
}
