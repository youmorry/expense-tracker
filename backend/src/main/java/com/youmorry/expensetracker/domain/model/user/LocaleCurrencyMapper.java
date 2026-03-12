package com.youmorry.expensetracker.domain.model.user;

import java.util.Map;

/** Google ID トークンの locale クレームから ISO 4217 通貨コードを推定する。 */
public final class LocaleCurrencyMapper {

  private static final String FALLBACK_CURRENCY = "USD";

  private static final Map<String, String> LOCALE_TO_CURRENCY =
      Map.ofEntries(
          Map.entry("ja", "JPY"),
          Map.entry("en-US", "USD"),
          Map.entry("en-GB", "GBP"),
          Map.entry("en-AU", "AUD"),
          Map.entry("en-CA", "CAD"),
          Map.entry("de", "EUR"),
          Map.entry("fr", "EUR"),
          Map.entry("it", "EUR"),
          Map.entry("es", "EUR"),
          Map.entry("nl", "EUR"),
          Map.entry("pt-BR", "BRL"),
          Map.entry("ko", "KRW"),
          Map.entry("zh-CN", "CNY"),
          Map.entry("zh-TW", "TWD"),
          Map.entry("th", "THB"),
          Map.entry("vi", "VND"),
          Map.entry("id", "IDR"),
          Map.entry("ms", "MYR"),
          Map.entry("hi", "INR"),
          Map.entry("ru", "RUB"),
          Map.entry("tr", "TRY"),
          Map.entry("pl", "PLN"),
          Map.entry("sv", "SEK"),
          Map.entry("da", "DKK"),
          Map.entry("nb", "NOK"),
          Map.entry("fi", "EUR"),
          Map.entry("en", "USD"));

  private LocaleCurrencyMapper() {}

  /** 指定された locale 文字列を ISO 4217 通貨コードに変換する。マッピングできない場合は USD を返す。 */
  public static String toCurrencyCode(String locale) {
    if (locale == null || locale.isBlank()) {
      return FALLBACK_CURRENCY;
    }

    // 完全一致を試行（例: "en-US"）
    String currency = LOCALE_TO_CURRENCY.get(locale);
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
