package com.youmorry.expensetracker.infrastructure.repository;

import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.transaction.Money;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import com.youmorry.expensetracker.domain.transaction.Transaction;
import com.youmorry.expensetracker.domain.transaction.TransactionId;
import com.youmorry.expensetracker.domain.transaction.TransactionSearchCriteria;
import com.youmorry.expensetracker.domain.transaction.TransactionSearchRepository;
import com.youmorry.expensetracker.domain.user.UserId;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** {@link TransactionSearchRepository} の JDBC 実装。動的クエリ構築に {@link JdbcClient} を使用する。 */
@Repository
public class JdbcTransactionSearchRepository implements TransactionSearchRepository {

  private final JdbcClient jdbcClient;

  /**
   * コンストラクタ。
   *
   * @param jdbcClient JDBC クライアント
   */
  public JdbcTransactionSearchRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  /**
   * 指定されたユーザーの支出を検索条件に基づいて取得する。
   *
   * @param userId ユーザー ID
   * @param criteria 検索条件
   * @return 検索条件に合致する支出記録のリスト（date DESC, id DESC）
   */
  @Override
  public List<Transaction> search(UserId userId, TransactionSearchCriteria criteria) {
    var sql = new StringBuilder("SELECT * FROM transactions WHERE user_id = :userId");
    var params = new MapSqlParameterSource("userId", userId.value());

    if (criteria.from() != null) {
      sql.append(" AND date >= :from");
      params.addValue("from", criteria.from());
    }
    if (criteria.to() != null) {
      sql.append(" AND date <= :to");
      params.addValue("to", criteria.to());
    }
    if (!criteria.categoryIds().isEmpty()) {
      sql.append(" AND category_id IN (:categoryIds)");
      params.addValue(
          "categoryIds", criteria.categoryIds().stream().map(CategoryId::value).toList());
    }
    if (criteria.needWantType() != null) {
      sql.append(" AND need_want_type = :needWantType");
      params.addValue("needWantType", criteria.needWantType().name());
    }
    if (criteria.keyword() != null) {
      sql.append(" AND (title ILIKE :keyword OR memo ILIKE :keyword)");
      params.addValue("keyword", "%" + criteria.keyword() + "%");
    }

    sql.append(" ORDER BY date DESC, id DESC");

    return jdbcClient
        .sql(sql.toString())
        .paramSource(params)
        .query(
            (rs, rowNum) ->
                new Transaction(
                    new TransactionId(rs.getLong("id")),
                    new UserId(rs.getLong("user_id")),
                    rs.getDate("date").toLocalDate(),
                    new Money(rs.getBigDecimal("amount")),
                    new CategoryId(rs.getLong("category_id")),
                    NeedWantType.valueOf(rs.getString("need_want_type")),
                    rs.getString("title"),
                    rs.getString("memo"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant()))
        .list();
  }
}
