package com.youmorry.expensetracker.infrastructure.repository;

import com.youmorry.expensetracker.domain.analytics.AnalyticsRepository;
import com.youmorry.expensetracker.domain.analytics.CategoryBreakdown;
import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.user.UserId;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** {@link AnalyticsRepository} の JDBC 実装。 */
@Repository
public class JdbcAnalyticsRepository implements AnalyticsRepository {

  private final JdbcClient jdbcClient;

  /**
   * コンストラクタ。
   *
   * @param jdbcClient JDBC クライアント
   */
  public JdbcAnalyticsRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  /**
   * {@inheritDoc}
   *
   * <p>{@code categories} を主テーブルにして LEFT JOIN することで、支出のないカテゴリも結果に含める。
   */
  @Override
  public List<CategoryBreakdown> findCategoryBreakdown(
      UserId userId, LocalDate from, LocalDate to) {
    var sql =
        new StringBuilder(
            """
            SELECT c.id AS category_id, c.name,
                   COALESCE(SUM(t.amount), 0) AS total_amount,
                   COUNT(t.id) AS transaction_count
            FROM categories c
            LEFT JOIN transactions t
              ON t.category_id = c.id
              AND t.user_id = :userId
            """);
    var params = new MapSqlParameterSource("userId", userId.value());

    if (from != null) {
      sql.append("  AND t.date >= :from\n");
      params.addValue("from", from);
    }
    if (to != null) {
      sql.append("  AND t.date <= :to\n");
      params.addValue("to", to);
    }

    sql.append("GROUP BY c.id, c.name, c.display_order\n");
    sql.append("ORDER BY total_amount DESC, c.display_order ASC");

    return jdbcClient
        .sql(sql.toString())
        .paramSource(params)
        .query(
            (rs, rowNum) ->
                new CategoryBreakdown(
                    new CategoryId(rs.getLong("category_id")),
                    rs.getString("name"),
                    rs.getBigDecimal("total_amount"),
                    rs.getLong("transaction_count")))
        .list();
  }
}
