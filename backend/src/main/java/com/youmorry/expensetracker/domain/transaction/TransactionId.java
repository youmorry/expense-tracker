package com.youmorry.expensetracker.domain.transaction;

/** 支出記録の識別子。 */
public record TransactionId(long value) {

  public TransactionId {
    if (value <= 0) {
      throw new IllegalArgumentException("TransactionId value must be positive, but was: " + value);
    }
  }
}
