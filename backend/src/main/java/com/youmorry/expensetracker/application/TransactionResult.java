package com.youmorry.expensetracker.application;

import com.youmorry.expensetracker.domain.transaction.Transaction;

/**
 * 支出登録の結果。
 *
 * @param transaction 保存されたトランザクション
 * @param categoryName カテゴリ名
 */
public record TransactionResult(Transaction transaction, String categoryName) {}
