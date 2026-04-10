package com.youmorry.expensetracker.user.domain;

/**
 * ユーザーの識別子。
 *
 * @param value ユーザー ID
 */
public record UserId(long value) {

  /**
   * ID が正の値であることを検証する。
   *
   * @throws IllegalArgumentException value が 0 以下の場合
   */
  public UserId {
    if (value <= 0) {
      throw new IllegalArgumentException("UserId value must be positive, but was: " + value);
    }
  }
}
