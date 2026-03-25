package com.youmorry.expensetracker.infrastructure.repository;

import com.youmorry.expensetracker.domain.model.transaction.Transaction;
import com.youmorry.expensetracker.domain.model.transaction.TransactionId;
import com.youmorry.expensetracker.domain.model.transaction.TransactionRepository;
import com.youmorry.expensetracker.domain.model.user.UserId;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JDBC による {@link TransactionRepository} の実装。 */
@Repository
public interface JdbcTransactionRepository
    extends TransactionRepository, CrudRepository<Transaction, TransactionId> {

  @Override
  @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY date DESC, id DESC")
  List<Transaction> findByUserId(UserId userId);
}
