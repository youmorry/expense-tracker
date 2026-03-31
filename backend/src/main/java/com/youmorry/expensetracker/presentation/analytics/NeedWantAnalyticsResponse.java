package com.youmorry.expensetracker.presentation.analytics;

import com.youmorry.expensetracker.application.analytics.NeedWantAnalyticsResult;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import java.math.BigDecimal;
import java.util.List;

/**
 * {@link NeedWantType}ごとの集計レスポンス。
 *
 * @param totalAmount 全区分の合計金額（文字列）
 * @param breakdown need/want 別内訳
 */
public record NeedWantAnalyticsResponse(String totalAmount, List<BreakdownItem> breakdown) {

  /**
   * {@link NeedWantAnalyticsResult} からレスポンス DTO を生成する。
   *
   * @param result need/want 別集計結果
   * @return レスポンス DTO
   */
  public static NeedWantAnalyticsResponse from(NeedWantAnalyticsResult result) {
    var items = result.breakdown().stream().map(BreakdownItem::from).toList();
    return new NeedWantAnalyticsResponse(result.totalAmount().toPlainString(), items);
  }

  /**
   * {@link NeedWantType}ごとの集計レスポンスの項目。
   *
   * @param type need/want 区分
   * @param amount 合計金額（文字列）
   * @param percentage 全体に占める割合（小数1桁）
   * @param transactionCount 支出件数
   */
  public record BreakdownItem(
      String type, String amount, BigDecimal percentage, long transactionCount) {

    static BreakdownItem from(NeedWantAnalyticsResult.Item item) {
      return new BreakdownItem(
          item.type().name(),
          item.amount().toPlainString(),
          item.percentage(),
          item.transactionCount());
    }
  }
}
