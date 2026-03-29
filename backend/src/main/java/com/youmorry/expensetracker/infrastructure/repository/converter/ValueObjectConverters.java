package com.youmorry.expensetracker.infrastructure.repository.converter;

import com.youmorry.expensetracker.domain.transaction.Money;
import java.math.BigDecimal;
import java.util.Currency;
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

  @ReadingConverter
  public enum StringToCurrency implements Converter<String, Currency> {
    INSTANCE;

    @Override
    public Currency convert(String source) {
      return Currency.getInstance(source);
    }
  }

  @WritingConverter
  public enum CurrencyToString implements Converter<Currency, String> {
    INSTANCE;

    @Override
    public String convert(Currency source) {
      return source.getCurrencyCode();
    }
  }
}
