package com.youmorry.expensetracker.infrastructure.repository;

import com.youmorry.expensetracker.infrastructure.repository.converter.IdConverters;
import com.youmorry.expensetracker.infrastructure.repository.converter.ValueObjectConverters;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

/** Spring Data JDBC のカスタム設定。カスタム型のコンバーターと Auditing を構成する。 */
@Configuration
@EnableJdbcAuditing(dateTimeProviderRef = "dateTimeProvider")
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

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  DateTimeProvider dateTimeProvider(Clock clock) {
    return () -> Optional.of(Instant.now(clock));
  }
}
