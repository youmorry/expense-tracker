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
  void toCurrencyCode_withBcp47LocaleIncludingRegion_returnsExpectedCurrency(
      String locale, String expected) {
    assertEquals(new CurrencyCode(expected), LocaleCurrencyMapper.toCurrencyCode(locale));
  }

  @Test
  void toCurrencyCode_withAcceptLanguageStyleLocale_resolvesCurrency() {
    // Accept-Language ヘッダーから抽出された BCP 47 タグ（ハイフン区切り）を処理できる
    assertEquals(new CurrencyCode("JPY"), LocaleCurrencyMapper.toCurrencyCode("ja-JP"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  ", "xx", "xx-YY", "invalid"})
  void toCurrencyCode_withUnknownOrBlankLocale_returnsUsd(String locale) {
    assertEquals(new CurrencyCode("USD"), LocaleCurrencyMapper.toCurrencyCode(locale));
  }

  @Test
  void toCurrencyCode_withLanguageOnlyLocale_returnsUsd() {
    // 地域コードなしの言語のみ（例: "ja"）は Currency.getInstance で解決できないため USD フォールバック
    assertEquals(new CurrencyCode("USD"), LocaleCurrencyMapper.toCurrencyCode("ja"));
  }
}
