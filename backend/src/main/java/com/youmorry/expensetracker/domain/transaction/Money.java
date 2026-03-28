package com.youmorry.expensetracker.domain.transaction;

import java.math.BigDecimal;
import java.util.Objects;

/** 金額を表す値オブジェクト。0 より大きい正の値のみ許容する。 */
public record Money(BigDecimal value) {

  /**
   * 金額が正の値であることを検証する。
   *
   * @throws NullPointerException value が null の場合
   * @throws IllegalArgumentException value が 0 以下の場合
   */
  public Money {
    Objects.requireNonNull(value, "value must not be null");
    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Money value must be positive, but was: " + value);
    }
  }
}
