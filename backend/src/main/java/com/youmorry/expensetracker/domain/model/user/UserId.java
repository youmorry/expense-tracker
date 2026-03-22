package com.youmorry.expensetracker.domain.model.user;

import java.util.Objects;

/** ユーザーの識別子。 */
public record UserId(Long value) {

  public UserId {
    Objects.requireNonNull(value, "value must not be null");
    if (value <= 0) {
      throw new IllegalArgumentException("UserId value must be positive, but was: " + value);
    }
  }
}
