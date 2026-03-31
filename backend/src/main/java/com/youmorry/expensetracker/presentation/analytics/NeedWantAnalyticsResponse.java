package com.youmorry.expensetracker.presentation.analytics;

import com.youmorry.expensetracker.application.analytics.NeedWantAnalyticsResult;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * {@link NeedWantType}ごとの集計レスポンス。
 *
 * @param totalAmount 全区分の合計金額（文字列）
 * @param breakdown need/want 別内訳
 */
public record NeedWantAnalyticsResponse(String totalAmount, List<BreakdownItem> breakdown) {

  /**
   * 不変条件を検証する。
   *
   * @throws NullPointerException totalAmount または breakdown が null の場合
   */
  public NeedWantAnalyticsResponse {
    Objects.requireNonNull(totalAmount, "totalAmount must not be null");
    Objects.requireNonNull(breakdown, "breakdown must not be null");
  }

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

    /**
     * 不変条件を検証する。
     *
     * @throws NullPointerException type、amount または percentage が null の場合
     * @throws IllegalArgumentException transactionCount が負の場合
     */
    public BreakdownItem {
      Objects.requireNonNull(type, "type must not be null");
      Objects.requireNonNull(amount, "amount must not be null");
      Objects.requireNonNull(percentage, "percentage must not be null");
      if (transactionCount < 0) {
        throw new IllegalArgumentException(
            "transactionCount must not be negative, but was: " + transactionCount);
      }
    }

    static BreakdownItem from(NeedWantAnalyticsResult.Item item) {
      return new BreakdownItem(
          item.type().name(),
          item.amount().toPlainString(),
          item.percentage(),
          item.transactionCount());
    }
  }
}
