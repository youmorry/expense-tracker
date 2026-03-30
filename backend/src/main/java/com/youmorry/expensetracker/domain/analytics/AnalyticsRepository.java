package com.youmorry.expensetracker.domain.analytics;

import com.youmorry.expensetracker.domain.user.UserId;
import java.time.LocalDate;
import java.util.List;

/** 分析・集計クエリを担うリポジトリインターフェース。 */
public interface AnalyticsRepository {

  /**
   * 指定されたユーザーのカテゴリ別集計を取得する。
   *
   * <p>支出のないカテゴリも amount=0、transactionCount=0 で含まれる。結果は amount 降順、同額の場合は display_order 昇順。
   *
   * @param userId ユーザー ID
   * @param from 集計開始日（null の場合は制限なし）
   * @param to 集計終了日（null の場合は制限なし）
   * @return カテゴリ別集計のリスト
   */
  List<CategoryBreakdown> findCategoryBreakdown(UserId userId, LocalDate from, LocalDate to);
}
