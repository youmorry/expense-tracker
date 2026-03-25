package com.youmorry.expensetracker.domain.transaction;

import com.youmorry.expensetracker.domain.user.UserId;
import java.util.List;
import java.util.Optional;

/** 支出記録の永続化を担うリポジトリインターフェース。 */
public interface TransactionRepository {

  /** 指定された ID の支出記録を取得する。 */
  Optional<Transaction> findById(TransactionId id);

  /** 指定されたユーザーの支出記録を全件取得する。 */
  List<Transaction> findByUserId(UserId userId);

  /** 支出記録を保存する。 */
  Transaction save(Transaction transaction);

  /** 指定された ID の支出記録を削除する。 */
  void deleteById(TransactionId id);
}
