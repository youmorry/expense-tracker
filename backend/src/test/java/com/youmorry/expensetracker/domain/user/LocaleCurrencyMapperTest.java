package com.youmorry.expensetracker.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Currency;
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
  void toCurrency_withLocaleIncludingRegion_returnsExpectedCurrency(
      String localeTag, String expected) {
    Locale locale = Locale.forLanguageTag(localeTag);

    assertEquals(Currency.getInstance(expected), LocaleCurrencyMapper.toCurrency(locale));
  }

  @Test
  void toCurrency_withNullLocale_returnsUsd() {
    assertEquals(Currency.getInstance("USD"), LocaleCurrencyMapper.toCurrency(null));
  }

  @ParameterizedTest
  @CsvSource({
    "ja, JPY", "ko, KRW", "zh, CNY", "hi, INR", "th, THB", "vi, VND", "tr, TRY", "ru, RUB",
    "pl, PLN", "sv, SEK", "da, DKK", "nb, NOK"
  })
  void toCurrency_withMappedLanguageOnlyLocale_returnsEstimatedCurrency(
      String languageTag, String expected) {
    Locale locale = Locale.forLanguageTag(languageTag);

    assertEquals(Currency.getInstance(expected), LocaleCurrencyMapper.toCurrency(locale));
  }

  @ParameterizedTest
  @CsvSource({"fr", "es", "pt", "ar", "de", "it", "nl", "en"})
  void toCurrency_withUnmappedLanguageOnlyLocale_returnsUsd(String languageTag) {
    Locale locale = Locale.forLanguageTag(languageTag);

    assertEquals(Currency.getInstance("USD"), LocaleCurrencyMapper.toCurrency(locale));
  }

  @Test
  void toCurrency_withUnknownCountryLocale_returnsUsd() {
    Locale unknown = Locale.forLanguageTag("xx-YY");

    assertEquals(Currency.getInstance("USD"), LocaleCurrencyMapper.toCurrency(unknown));
  }
}
