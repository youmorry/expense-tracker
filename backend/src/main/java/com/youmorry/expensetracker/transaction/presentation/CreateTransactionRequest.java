package com.youmorry.expensetracker.transaction.presentation;

import com.youmorry.expensetracker.transaction.application.TransactionCreateCommand;
import com.youmorry.expensetracker.category.domain.CategoryId;
import com.youmorry.expensetracker.transaction.domain.NeedWantType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/** 支出登録リクエスト DTO。 */
public record CreateTransactionRequest(
    @NotNull LocalDate date,
    @NotNull BigDecimal amount,
    @Nullable @Min(1) Long categoryId,
    @Nullable NeedWantType needWantType,
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
        categoryId != null ? new CategoryId(categoryId) : null,
        needWantType,
        title,
        memo);
  }
}
