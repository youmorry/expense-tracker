package com.youmorry.expensetracker.presentation.analytics;

import com.youmorry.expensetracker.application.analytics.CategoryAnalyticsResult;
import java.math.BigDecimal;
import java.util.List;

/**
 * カテゴリ別集計のレスポンス DTO。
 *
 * @param totalAmount 全カテゴリの合計金額（文字列）
 * @param categories カテゴリ別内訳
 */
public record CategoryAnalyticsResponse(String totalAmount, List<CategoryItem> categories) {

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
