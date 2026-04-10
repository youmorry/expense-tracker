package com.youmorry.expensetracker.domain.analytics;

import com.youmorry.expensetracker.user.domain.UserId;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

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
  List<CategoryBreakdown> findCategoryBreakdown(
      UserId userId, @Nullable LocalDate from, @Nullable LocalDate to);

  /**
   * 指定されたユーザーの need/want/unset 別集計を取得する。
   *
   * <p>NEED / WANT / UNSET の 3 種類は該当データが 0 件でも結果に含まれる。
   *
   * @param userId ユーザー ID
   * @param from 集計開始日（null の場合は制限なし）
   * @param to 集計終了日（null の場合は制限なし）
   * @return need/want/unset 別集計のリスト
   */
  List<NeedWantBreakdown> findNeedWantBreakdown(
      UserId userId, @Nullable LocalDate from, @Nullable LocalDate to);
}
