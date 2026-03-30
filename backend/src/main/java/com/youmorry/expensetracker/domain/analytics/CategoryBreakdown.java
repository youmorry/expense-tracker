package com.youmorry.expensetracker.domain.analytics;

import com.youmorry.expensetracker.domain.category.CategoryId;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * カテゴリ別集計結果を表す値オブジェクト。
 *
 * @param categoryId カテゴリ ID
 * @param name カテゴリ名
 * @param amount 合計金額
 * @param transactionCount 支出件数
 */
public record CategoryBreakdown(
    CategoryId categoryId, String name, BigDecimal amount, long transactionCount) {

  /** 各フィールドが null でないことを検証する。 */
  public CategoryBreakdown {
    Objects.requireNonNull(categoryId, "categoryId must not be null");
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
  }
}
