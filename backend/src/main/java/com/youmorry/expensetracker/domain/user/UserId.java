package com.youmorry.expensetracker.domain.user;

/** ユーザーの識別子。 */
public record UserId(long value) {

  public UserId {
    if (value <= 0) {
      throw new IllegalArgumentException("UserId value must be positive, but was: " + value);
    }
  }
}
