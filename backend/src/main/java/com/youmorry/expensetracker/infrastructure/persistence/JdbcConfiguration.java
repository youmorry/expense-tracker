package com.youmorry.expensetracker.infrastructure.persistence;

import com.youmorry.expensetracker.infrastructure.persistence.converter.IdConverters;
import com.youmorry.expensetracker.infrastructure.persistence.converter.ValueObjectConverters;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

/** Spring Data JDBC のカスタム設定。カスタム型のコンバーターを登録する。 */
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
        IdConverters.TransactionIdToLong.INSTANCE,
        ValueObjectConverters.BigDecimalToMoney.INSTANCE,
        ValueObjectConverters.MoneyToBigDecimal.INSTANCE,
        ValueObjectConverters.StringToCurrencyCode.INSTANCE,
        ValueObjectConverters.CurrencyCodeToString.INSTANCE);
  }
}
