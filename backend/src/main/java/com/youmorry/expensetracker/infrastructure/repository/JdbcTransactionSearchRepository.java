package com.youmorry.expensetracker.infrastructure.repository;

import com.youmorry.expensetracker.domain.transaction.Transaction;
import com.youmorry.expensetracker.domain.transaction.TransactionSearchCriteria;
import com.youmorry.expensetracker.domain.transaction.TransactionSearchRepository;
import com.youmorry.expensetracker.domain.user.UserId;
import java.util.List;
import org.springframework.stereotype.Repository;

/** {@link TransactionSearchRepository} の JDBC 実装。 */
@Repository
public class JdbcTransactionSearchRepository implements TransactionSearchRepository {

  @Override
  public List<Transaction> search(UserId userId, TransactionSearchCriteria criteria) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
