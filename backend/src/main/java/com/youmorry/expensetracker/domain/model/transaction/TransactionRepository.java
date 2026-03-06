package com.youmorry.expensetracker.domain.model.transaction;

import com.youmorry.expensetracker.domain.model.user.UserId;
import java.util.List;
import java.util.Optional;

/** 支出記録の永続化を担うリポジトリインターフェース。 */
public interface TransactionRepository {

  Optional<Transaction> findById(TransactionId id);

  List<Transaction> findByUserId(UserId userId);

  Transaction save(Transaction transaction);

  void deleteById(TransactionId id);
}
