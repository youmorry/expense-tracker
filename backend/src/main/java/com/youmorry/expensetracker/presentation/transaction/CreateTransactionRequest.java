package com.youmorry.expensetracker.presentation.transaction;

import com.youmorry.expensetracker.application.TransactionCreateCommand;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/** 支出登録リクエスト DTO。 */
public record CreateTransactionRequest(
    @NotNull LocalDate date,
    @NotNull BigDecimal amount,
    @Nullable Long categoryId,
    @Nullable String needWantType,
    @Nullable @Size(max = 200) String title,
    @Nullable @Size(max = 2000) String memo) {

  /**
   * アプリケーション層のコマンドに変換する。
   *
   * @return 支出登録コマンド
   */
  public TransactionCreateCommand toCommand() {
    return new TransactionCreateCommand(
        date,
        amount,
        categoryId,
        needWantType != null ? NeedWantType.valueOf(needWantType) : null,
        title,
        memo);
  }
}
