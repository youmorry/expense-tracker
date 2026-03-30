package com.youmorry.expensetracker.presentation.transaction;

import com.youmorry.expensetracker.application.TransactionUpdateCommand;
import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/** 支出更新リクエスト DTO。 */
public record UpdateTransactionRequest(
    @NotNull LocalDate date,
    @NotNull BigDecimal amount,
    @Nullable Long categoryId,
    @Nullable NeedWantType needWantType,
    @Nullable @Size(max = 200) String title,
    @Nullable @Size(max = 2000) String memo) {

  /**
   * アプリケーション層のコマンドに変換する。
   *
   * @return 支出更新コマンド
   */
  public TransactionUpdateCommand toCommand() {
    return new TransactionUpdateCommand(
        date,
        amount,
        categoryId != null ? new CategoryId(categoryId) : null,
        needWantType,
        title,
        memo);
  }
}
