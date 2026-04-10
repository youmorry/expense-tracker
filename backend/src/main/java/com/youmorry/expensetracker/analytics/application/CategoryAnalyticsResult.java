package com.youmorry.expensetracker.analytics.application;

import com.youmorry.expensetracker.category.domain.CategoryId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * カテゴリ別集計の結果オブジェクト。
 *
 * @param totalAmount 全カテゴリの合計金額
 * @param breakdown カテゴリ別の内訳（percentage を含む）
 */
public record CategoryAnalyticsResult(BigDecimal totalAmount, List<Item> breakdown) {

  /** 不変条件を検証する。 */
  public CategoryAnalyticsResult {
    Objects.requireNonNull(totalAmount, "totalAmount must not be null");
    Objects.requireNonNull(breakdown, "breakdown must not be null");
  }

  /**
   * カテゴリ別内訳の1項目。
   *
   * @param categoryId カテゴリ ID
   * @param name カテゴリ名
   * @param amount 合計金額
   * @param transactionCount 支出件数
   * @param percentage 全体に占める割合（小数1桁）
   */
  public record Item(
      CategoryId categoryId,
      String name,
      BigDecimal amount,
      long transactionCount,
      BigDecimal percentage) {

    /** 不変条件を検証する。 */
    public Item {
      Objects.requireNonNull(categoryId, "categoryId must not be null");
      Objects.requireNonNull(name, "name must not be null");
      Objects.requireNonNull(amount, "amount must not be null");
      Objects.requireNonNull(percentage, "percentage must not be null");
    }
  }
}
