package com.youmorry.expensetracker.domain.transaction;

import com.youmorry.expensetracker.domain.category.CategoryId;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * 支出検索の条件を保持する。すべてのフィールドはオプショナルで、指定された条件のみ AND で適用される。
 *
 * @param from 取得開始日（この日付を含む）
 * @param to 取得終了日（この日付を含む）
 * @param categoryIds カテゴリ ID のリスト（空リスト = フィルターなし）
 * @param needWantType 必要/欲しい区分
 * @param keyword title, memo の部分一致検索キーワード
 */
public record TransactionSearchCriteria(
    @Nullable LocalDate from,
    @Nullable LocalDate to,
    List<CategoryId> categoryIds,
    @Nullable NeedWantType needWantType,
    @Nullable String keyword) {

  /**
   * 不変条件を検証する。
   *
   * @throws NullPointerException categoryIds が null の場合
   */
  public TransactionSearchCriteria {
    Objects.requireNonNull(categoryIds, "categoryIds must not be null");
  }
}
