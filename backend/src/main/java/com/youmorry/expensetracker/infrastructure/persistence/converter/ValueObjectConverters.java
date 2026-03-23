package com.youmorry.expensetracker.infrastructure.persistence.converter;

import com.youmorry.expensetracker.domain.model.user.CurrencyCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

/** Spring Data JDBC 用の値オブジェクトコンバーター。 */
public final class ValueObjectConverters {

  private ValueObjectConverters() {}

  @ReadingConverter
  public enum StringToCurrencyCode implements Converter<String, CurrencyCode> {
    INSTANCE;

    @Override
    public CurrencyCode convert(String source) {
      return new CurrencyCode(source);
    }
  }

  @WritingConverter
  public enum CurrencyCodeToString implements Converter<CurrencyCode, String> {
    INSTANCE;

    @Override
    public String convert(CurrencyCode source) {
      return source.value();
    }
  }
}
