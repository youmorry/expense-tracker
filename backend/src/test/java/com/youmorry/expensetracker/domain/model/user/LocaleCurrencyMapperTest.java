package com.youmorry.expensetracker.domain.model.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LocaleCurrencyMapperTest {

  @ParameterizedTest
  @CsvSource({
    "ja, JPY",
    "en-US, USD",
    "en-GB, GBP",
    "de, EUR",
    "fr, EUR",
    "zh-CN, CNY",
    "zh-TW, TWD",
    "ko, KRW",
    "pt-BR, BRL",
    "en, USD"
  })
  void toCurrencyCode_withKnownLocale_returnsExpectedCurrency(String locale, String expected) {
    assertEquals(new CurrencyCode(expected), LocaleCurrencyMapper.toCurrencyCode(locale));
  }

  @Test
  void toCurrencyCode_withLanguageOnlyFallback_returnsLanguageCurrency() {
    // "ja-JP" は完全一致しないが、言語部分 "ja" で JPY にマッチする
    assertEquals(new CurrencyCode("JPY"), LocaleCurrencyMapper.toCurrencyCode("ja-JP"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  ", "xx", "xx-YY"})
  void toCurrencyCode_withUnknownOrBlankLocale_returnsUsd(String locale) {
    assertEquals(new CurrencyCode("USD"), LocaleCurrencyMapper.toCurrencyCode(locale));
  }
}
