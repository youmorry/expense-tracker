package com.youmorry.expensetracker.domain.model.user;

import java.util.Map;

/** Google ID トークンの locale クレームから ISO 4217 通貨コードを推定する。 */
public final class LocaleCurrencyMapper {

  private static final Map<String, CurrencyCode> LOCALE_TO_CURRENCY =
      Map.ofEntries(
          Map.entry("ja", CurrencyCode.JPY),
          Map.entry("en-US", CurrencyCode.USD),
          Map.entry("en-GB", CurrencyCode.GBP),
          Map.entry("en-AU", CurrencyCode.AUD),
          Map.entry("en-CA", CurrencyCode.CAD),
          Map.entry("de", CurrencyCode.EUR),
          Map.entry("fr", CurrencyCode.EUR),
          Map.entry("it", CurrencyCode.EUR),
          Map.entry("es", CurrencyCode.EUR),
          Map.entry("nl", CurrencyCode.EUR),
          Map.entry("pt-BR", CurrencyCode.BRL),
          Map.entry("ko", CurrencyCode.KRW),
          Map.entry("zh-CN", CurrencyCode.CNY),
          Map.entry("zh-TW", CurrencyCode.TWD),
          Map.entry("th", CurrencyCode.THB),
          Map.entry("vi", CurrencyCode.VND),
          Map.entry("id", CurrencyCode.IDR),
          Map.entry("ms", CurrencyCode.MYR),
          Map.entry("hi", CurrencyCode.INR),
          Map.entry("ru", CurrencyCode.RUB),
          Map.entry("tr", CurrencyCode.TRY),
          Map.entry("pl", CurrencyCode.PLN),
          Map.entry("sv", CurrencyCode.SEK),
          Map.entry("da", CurrencyCode.DKK),
          Map.entry("nb", CurrencyCode.NOK),
          Map.entry("fi", CurrencyCode.EUR),
          Map.entry("en", CurrencyCode.USD));

  private LocaleCurrencyMapper() {}

  /** 指定された locale 文字列を ISO 4217 通貨コードに変換する。マッピングできない場合は USD を返す。 */
  public static CurrencyCode toCurrencyCode(String locale) {
    if (locale == null || locale.isBlank()) {
      return CurrencyCode.USD;
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

    return CurrencyCode.USD;
  }
}
