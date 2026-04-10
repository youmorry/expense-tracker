package com.youmorry.expensetracker.shared.infrastructure.persistence;

import com.youmorry.expensetracker.domain.transaction.Money;
import java.math.BigDecimal;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

/** Spring Data JDBC 用の値オブジェクトコンバーター。 */
@SuppressWarnings("checkstyle:MissingJavadocType")
public final class ValueObjectConverters {

  private ValueObjectConverters() {}

  @ReadingConverter
  public enum BigDecimalToMoney implements Converter<BigDecimal, Money> {
    INSTANCE;

    @Override
    public Money convert(BigDecimal source) {
      return new Money(source);
    }
  }

  @WritingConverter
  public enum MoneyToBigDecimal implements Converter<Money, BigDecimal> {
    INSTANCE;

    @Override
    public BigDecimal convert(Money source) {
      return source.value();
    }
  }
}
