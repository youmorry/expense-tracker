package com.youmorry.expensetracker.domain.model.user;

import java.util.Map;

/** Google ID トークンの locale クレームから ISO 4217 通貨コードを推定する。 */
public final class LocaleCurrencyMapper {

  private static final CurrencyCode FALLBACK_CURRENCY = new CurrencyCode("USD");

  private static final Map<String, CurrencyCode> LOCALE_TO_CURRENCY =
      Map.ofEntries(
          Map.entry("ja", new CurrencyCode("JPY")),
          Map.entry("en-US", new CurrencyCode("USD")),
          Map.entry("en-GB", new CurrencyCode("GBP")),
          Map.entry("en-AU", new CurrencyCode("AUD")),
          Map.entry("en-CA", new CurrencyCode("CAD")),
          Map.entry("de", new CurrencyCode("EUR")),
          Map.entry("fr", new CurrencyCode("EUR")),
          Map.entry("it", new CurrencyCode("EUR")),
          Map.entry("es", new CurrencyCode("EUR")),
          Map.entry("nl", new CurrencyCode("EUR")),
          Map.entry("pt-BR", new CurrencyCode("BRL")),
          Map.entry("ko", new CurrencyCode("KRW")),
          Map.entry("zh-CN", new CurrencyCode("CNY")),
          Map.entry("zh-TW", new CurrencyCode("TWD")),
          Map.entry("th", new CurrencyCode("THB")),
          Map.entry("vi", new CurrencyCode("VND")),
          Map.entry("id", new CurrencyCode("IDR")),
          Map.entry("ms", new CurrencyCode("MYR")),
          Map.entry("hi", new CurrencyCode("INR")),
          Map.entry("ru", new CurrencyCode("RUB")),
          Map.entry("tr", new CurrencyCode("TRY")),
          Map.entry("pl", new CurrencyCode("PLN")),
          Map.entry("sv", new CurrencyCode("SEK")),
          Map.entry("da", new CurrencyCode("DKK")),
          Map.entry("nb", new CurrencyCode("NOK")),
          Map.entry("fi", new CurrencyCode("EUR")),
          Map.entry("en", new CurrencyCode("USD")));

  private LocaleCurrencyMapper() {}

  /** 指定された locale 文字列を ISO 4217 通貨コードに変換する。マッピングできない場合は USD を返す。 */
  public static CurrencyCode toCurrencyCode(String locale) {
    if (locale == null || locale.isBlank()) {
      return FALLBACK_CURRENCY;
    }

    // 完全一致を試行（例: "en-US"）
    CurrencyCode currency = LOCALE_TO_CURRENCY.get(locale);
    if (currency != null) {
      return currency;
    }

    // 言語部分のみで再試行（例: "ja-JP" → "ja"）
    int dashIndex = locale.indexOf('-');
    if (dashIndex > 0) {
      currency = LOCALE_TO_CURRENCY.get(locale.substring(0, dashIndex));
      if (currency != null) {
        return currency;
      }
    }

    return FALLBACK_CURRENCY;
  }
}
