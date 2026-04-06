package com.youmorry.expensetracker.presentation.transaction;

import com.youmorry.expensetracker.application.transaction.TransactionResult;
import com.youmorry.expensetracker.domain.transaction.Transaction;
import java.time.Instant;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/** 支出レスポンス DTO。 */
public record TransactionResponse(
    Long id,
    LocalDate date,
    String amount,
    Long categoryId,
    String categoryName,
    String needWantType,
    @Nullable String title,
    @Nullable String memo,
    Instant createdAt,
    Instant updatedAt) {

  /**
   * TransactionResult からレスポンスを生成する。
   *
   * @param result 支出登録結果
   * @return 支出レスポンス
   */
  public static TransactionResponse from(TransactionResult result) {
    Transaction tx = result.transaction();
    return new TransactionResponse(
        tx.id().value(),
        tx.date(),
        tx.amount().value().toPlainString(),
        tx.categoryId().value(),
        result.categoryName(),
        tx.needWantType().name(),
        tx.title(),
        tx.memo(),
        tx.createdAt(),
        tx.updatedAt());
  }
}
