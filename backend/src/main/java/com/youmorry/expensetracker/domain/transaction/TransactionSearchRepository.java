package com.youmorry.expensetracker.domain.transaction;

import com.youmorry.expensetracker.user.domain.UserId;
import java.util.List;

/** 支出の条件付き検索を担うリポジトリインターフェース。 */
public interface TransactionSearchRepository {

  /**
   * 指定されたユーザーの支出を検索条件に基づいて取得する。結果は date DESC, created_at DESC でソートされる。
   *
   * @param userId ユーザー ID
   * @param criteria 検索条件
   * @return 検索条件に合致する支出記録のリスト
   */
  List<Transaction> search(UserId userId, TransactionSearchCriteria criteria);
}
