package com.youmorry.expensetracker.domain.model.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LocaleCurrencyMapperTest {

  @ParameterizedTest
  @CsvSource({
    "ja-JP, JPY",
    "en-US, USD",
    "en-GB, GBP",
    "de-DE, EUR",
    "fr-FR, EUR",
    "zh-CN, CNY",
    "zh-TW, TWD",
    "ko-KR, KRW",
    "pt-BR, BRL",
    "en-AU, AUD",
    "en-CA, CAD",
    "th-TH, THB",
    "vi-VN, VND",
    "id-ID, IDR",
    "ms-MY, MYR",
    "hi-IN, INR",
    "ru-RU, RUB",
    "tr-TR, TRY",
    "pl-PL, PLN",
    "sv-SE, SEK",
    "da-DK, DKK",
    "nb-NO, NOK",
    "fi-FI, EUR"
  })
  void toCurrencyCode_withLocaleIncludingRegion_returnsExpectedCurrency(
      String localeTag, String expected) {
    Locale locale = Locale.forLanguageTag(localeTag);

    assertEquals(new CurrencyCode(expected), LocaleCurrencyMapper.toCurrencyCode(locale));
  }

  @Test
  void toCurrencyCode_withNullLocale_returnsUsd() {
    assertEquals(CurrencyCode.USD, LocaleCurrencyMapper.toCurrencyCode(null));
  }

  @Test
  void toCurrencyCode_withLanguageOnlyLocale_returnsUsd() {
    assertEquals(CurrencyCode.USD, LocaleCurrencyMapper.toCurrencyCode(Locale.JAPANESE));
  }

  @Test
  void toCurrencyCode_withUnknownCountryLocale_returnsUsd() {
    Locale unknown = Locale.forLanguageTag("xx-YY");

    assertEquals(CurrencyCode.USD, LocaleCurrencyMapper.toCurrencyCode(unknown));
  }
}
