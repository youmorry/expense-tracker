package com.youmorry.expensetracker.domain.transaction;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 金額を表す値オブジェクト。
 *
 * @param value 金額
 */
public record Money(BigDecimal value) {

  /**
   * 金額の値が null でないことを検証する。
   *
   * @throws NullPointerException value が null の場合
   */
  public Money {
    Objects.requireNonNull(value, "value must not be null");
  }
}
