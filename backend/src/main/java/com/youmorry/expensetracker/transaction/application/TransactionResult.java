package com.youmorry.expensetracker.transaction.application;

import com.youmorry.expensetracker.transaction.domain.Transaction;
import java.util.Objects;

/**
 * 支出登録の結果。
 *
 * @param transaction 保存されたトランザクション
 * @param categoryName カテゴリ名
 */
public record TransactionResult(Transaction transaction, String categoryName) {

  /** 不変条件を検証する。 */
  public TransactionResult {
    Objects.requireNonNull(transaction, "transaction must not be null");
    Objects.requireNonNull(categoryName, "categoryName must not be null");
  }
}
