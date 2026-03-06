package com.youmorry.expensetracker.domain.model.transaction;

import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.data.relational.core.mapping.Column;

/** 金額を表す値オブジェクト。0 より大きい正の値のみ許容する。 */
public record Money(@Column("amount") BigDecimal value) {

  public Money {
    Objects.requireNonNull(value, "value must not be null");
    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Money value must be positive, but was: " + value);
    }
  }
}
