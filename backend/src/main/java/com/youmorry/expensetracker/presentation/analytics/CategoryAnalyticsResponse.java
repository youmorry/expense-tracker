package com.youmorry.expensetracker.presentation.analytics;

import com.youmorry.expensetracker.application.analytics.CategoryAnalyticsResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * カテゴリ別集計のレスポンス DTO。
 *
 * @param totalAmount 全カテゴリの合計金額（文字列）
 * @param categories カテゴリ別内訳
 */
public record CategoryAnalyticsResponse(String totalAmount, List<CategoryItem> categories) {

  /**
   * 不変条件を検証する。
   *
   * @throws NullPointerException totalAmount または categories が null の場合
   */
  public CategoryAnalyticsResponse {
    Objects.requireNonNull(totalAmount, "totalAmount must not be null");
    Objects.requireNonNull(categories, "categories must not be null");
  }

  /**
   * {@link CategoryAnalyticsResult} からレスポンス DTO を生成する。
   *
   * @param result カテゴリ別集計結果
   * @return レスポンス DTO
   */
  public static CategoryAnalyticsResponse from(CategoryAnalyticsResult result) {
    var categories = result.breakdown().stream().map(CategoryItem::from).toList();
    return new CategoryAnalyticsResponse(result.totalAmount().toPlainString(), categories);
  }

  /**
   * カテゴリ別内訳の1項目。
   *
   * @param categoryId カテゴリ ID
   * @param categoryName カテゴリ名
   * @param amount 合計金額（文字列）
   * @param percentage 全体に占める割合
   * @param transactionCount 支出件数
   */
  public record CategoryItem(
      Long categoryId,
      String categoryName,
      String amount,
      BigDecimal percentage,
      long transactionCount) {

    /**
     * 不変条件を検証する。
     *
     * @throws NullPointerException categoryName、amount または percentage が null の場合
     * @throws IllegalArgumentException transactionCount が負の場合
     */
    public CategoryItem {
      Objects.requireNonNull(categoryName, "categoryName must not be null");
      Objects.requireNonNull(amount, "amount must not be null");
      Objects.requireNonNull(percentage, "percentage must not be null");
      if (transactionCount < 0) {
        throw new IllegalArgumentException(
            "transactionCount must not be negative, but was: " + transactionCount);
      }
    }

    static CategoryItem from(CategoryAnalyticsResult.Item item) {
      return new CategoryItem(
          item.categoryId().value(),
          item.name(),
          item.amount().toPlainString(),
          item.percentage(),
          item.transactionCount());
    }
  }
}
