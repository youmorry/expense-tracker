package com.youmorry.expensetracker.analytics.domain;

import com.youmorry.expensetracker.transaction.domain.NeedWantType;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * {@link NeedWantType}の集計結果。
 *
 * @param type need/want 区分
 * @param amount 合計金額
 * @param transactionCount 支出件数
 */
public record NeedWantBreakdown(NeedWantType type, BigDecimal amount, long transactionCount) {

  /** 不変条件を検証する。 */
  public NeedWantBreakdown {
    Objects.requireNonNull(type, "type must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    if (transactionCount < 0) {
      throw new IllegalArgumentException(
          "transactionCount must be non-negative, but was: " + transactionCount);
    }
  }
}
