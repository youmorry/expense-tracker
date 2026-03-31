package com.youmorry.expensetracker.application.transaction;

import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import com.youmorry.expensetracker.domain.transaction.TransactionSearchCriteria;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * 支出検索のクエリ。
 *
 * @param from 取得開始日（この日付を含む）
 * @param to 取得終了日（この日付を含む）
 * @param categoryIds カテゴリ ID のリスト（空リスト = フィルターなし）
 * @param needWantType 必要/欲しい区分
 * @param keyword title, memo の部分一致検索キーワード
 */
public record TransactionSearchQuery(
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
  public TransactionSearchQuery {
    Objects.requireNonNull(categoryIds, "categoryIds must not be null");
  }

  /**
   * ドメイン層の検索条件に変換する。
   *
   * @return 検索条件
   */
  public TransactionSearchCriteria toCriteria() {
    return new TransactionSearchCriteria(from, to, categoryIds, needWantType, keyword);
  }
}
