package com.youmorry.expensetracker.application;

import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * 支出登録のコマンド。
 *
 * @param date 支出日
 * @param amount 金額（文字列形式）
 * @param categoryId カテゴリ ID（未指定の場合は Uncategorized）
 * @param needWantType 必要/欲しい区分（未指定の場合は UNSET）
 * @param title タイトル
 * @param memo メモ
 */
public record TransactionCreateCommand(
    LocalDate date,
    String amount,
    @Nullable Long categoryId,
    @Nullable NeedWantType needWantType,
    @Nullable String title,
    @Nullable String memo) {

  /** 必須フィールドの不変条件を検証する。 */
  public TransactionCreateCommand {
    Objects.requireNonNull(date, "date must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
  }
}
