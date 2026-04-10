package com.youmorry.expensetracker.transaction.domain;

import com.youmorry.expensetracker.user.domain.UserId;
import java.util.List;
import java.util.Optional;

/** 支出記録の永続化を担うリポジトリインターフェース。 */
public interface TransactionRepository {

  /**
   * 指定された ID の支出記録を取得する。
   *
   * @param id 支出記録 ID
   * @return 支出記録。存在しない場合は空
   */
  Optional<Transaction> findById(TransactionId id);

  /**
   * 指定されたユーザーの支出記録を全件取得する。
   *
   * @param userId ユーザー ID
   * @return 支出記録のリスト
   */
  List<Transaction> findByUserId(UserId userId);

  /**
   * 支出記録を保存する。
   *
   * @param transaction 保存する支出記録
   * @return 保存された支出記録（ID 採番済み）
   */
  Transaction save(Transaction transaction);

  /**
   * 指定された ID の支出記録を削除する。
   *
   * @param id 削除する支出記録の ID
   */
  void deleteById(TransactionId id);
}
