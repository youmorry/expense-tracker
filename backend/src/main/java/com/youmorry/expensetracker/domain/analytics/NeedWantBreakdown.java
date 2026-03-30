package com.youmorry.expensetracker.domain.analytics;

import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * need/want 別集計結果を表す値オブジェクト。
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
