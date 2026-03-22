package com.youmorry.expensetracker.domain.model.transaction;

import java.util.Objects;

/** 支出記録の識別子。 */
public record TransactionId(Long value) {

  public TransactionId {
    Objects.requireNonNull(value, "value must not be null");
    if (value <= 0) {
      throw new IllegalArgumentException(
          "TransactionId value must be positive, but was: " + value);
    }
  }
}
