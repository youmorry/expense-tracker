package com.youmorry.expensetracker.infrastructure.persistence;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

/** Spring Data JDBC のカスタム設定。ID 型のコンバーターを登録する。 */
@Configuration
public class JdbcConfiguration extends AbstractJdbcConfiguration {

  @Override
  protected List<?> userConverters() {
    return List.of(
        IdConverters.LongToCategoryId.INSTANCE,
        IdConverters.CategoryIdToLong.INSTANCE,
        IdConverters.LongToUserId.INSTANCE,
        IdConverters.UserIdToLong.INSTANCE,
        IdConverters.LongToTransactionId.INSTANCE,
        IdConverters.TransactionIdToLong.INSTANCE);
  }
}
