package com.youmorry.expensetracker.analytics.application;

import com.youmorry.expensetracker.transaction.domain.NeedWantType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * {@link NeedWantType}ごとの集計結果。
 *
 * @param totalAmount 全区分の合計金額
 * @param breakdown need/want 別の内訳（percentage を含む）
 */
public record NeedWantAnalyticsResult(BigDecimal totalAmount, List<Item> breakdown) {

  /** 不変条件を検証する。 */
  public NeedWantAnalyticsResult {
    Objects.requireNonNull(totalAmount, "totalAmount must not be null");
    Objects.requireNonNull(breakdown, "breakdown must not be null");
  }

  /**
   * {@link NeedWantType}ごとの集計結果の項目。
   *
   * @param type need/want 区分
   * @param amount 合計金額
   * @param transactionCount 支出件数
   * @param percentage 全体に占める割合（小数1桁）
   */
  public record Item(
      NeedWantType type, BigDecimal amount, long transactionCount, BigDecimal percentage) {

    /** 不変条件を検証する。 */
    public Item {
      Objects.requireNonNull(type, "type must not be null");
      Objects.requireNonNull(amount, "amount must not be null");
      Objects.requireNonNull(percentage, "percentage must not be null");
    }
  }
}
