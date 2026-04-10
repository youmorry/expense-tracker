package com.youmorry.expensetracker.transaction.presentation;

import com.youmorry.expensetracker.transaction.application.TransactionResult;
import java.util.List;

/**
 * 支出一覧のレスポンス DTO。
 *
 * @param items 支出レスポンスのリスト
 */
public record TransactionListResponse(List<TransactionResponse> items) {

  /**
   * {@link TransactionResult} のリストから {@link TransactionListResponse} を生成する。
   *
   * @param results 支出検索結果のリスト
   * @return 支出一覧レスポンス
   */
  public static TransactionListResponse from(List<TransactionResult> results) {
    return new TransactionListResponse(results.stream().map(TransactionResponse::from).toList());
  }
}
