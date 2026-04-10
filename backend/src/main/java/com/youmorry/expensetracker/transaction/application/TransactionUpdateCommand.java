package com.youmorry.expensetracker.transaction.application;

import com.youmorry.expensetracker.category.domain.CategoryId;
import com.youmorry.expensetracker.transaction.domain.NeedWantType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * 支出更新のコマンド。
 *
 * @param date 支出日
 * @param amount 金額
 * @param categoryId カテゴリ ID（未指定の場合は Uncategorized）
 * @param needWantType 必要/欲しい区分（未指定の場合は UNSET）
 * @param title タイトル
 * @param memo メモ
 */
public record TransactionUpdateCommand(
    LocalDate date,
    BigDecimal amount,
    @Nullable CategoryId categoryId,
    @Nullable NeedWantType needWantType,
    @Nullable String title,
    @Nullable String memo) {

  /** 必須フィールドの不変条件を検証する。 */
  public TransactionUpdateCommand {
    Objects.requireNonNull(date, "date must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
  }
}
