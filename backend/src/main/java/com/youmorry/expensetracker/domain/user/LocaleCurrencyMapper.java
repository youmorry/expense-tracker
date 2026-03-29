package com.youmorry.expensetracker.domain.user;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/** {@link Locale} から ISO 4217 通貨コードを推定する。 */
public final class LocaleCurrencyMapper {

  /**
   * 言語コード → 代表国コードの静的マッピング。
   *
   * <p>「1言語 = 1つの代表的な通貨」が明確な言語のみ対象とする。 複数国で使われる言語（fr, es, pt, ar, de 等）は除外。
   */
  private static final Map<String, String> LANGUAGE_TO_COUNTRY =
      Map.ofEntries(
          Map.entry("ja", "JP"),
          Map.entry("ko", "KR"),
          Map.entry("zh", "CN"),
          Map.entry("hi", "IN"),
          Map.entry("th", "TH"),
          Map.entry("vi", "VN"),
          Map.entry("tr", "TR"),
          Map.entry("ru", "RU"),
          Map.entry("pl", "PL"),
          Map.entry("sv", "SE"),
          Map.entry("da", "DK"),
          Map.entry("nb", "NO"));

  private LocaleCurrencyMapper() {}

  /**
   * 指定された {@link Locale} を ISO 4217 通貨コードに変換する。
   *
   * <p>{@link Currency#getInstance(Locale)} を使用して JDK の locale-通貨マッピングで解決する。 国コードを持たない Locale
   * の場合、言語コードから代表国コードを推定して解決を試みる。 解決できない場合は USD を返す。
   *
   * @param locale ロケール（例: {@code Locale.of("ja", "JP")}）。null 可
   * @return 推定された通貨コード
   */
  public static CurrencyCode toCurrencyCode(@Nullable Locale locale) {
    if (locale == null) {
      return CurrencyCode.USD;
    }

    Locale resolved = locale;
    if (locale.getCountry().isEmpty()) {
      String country = LANGUAGE_TO_COUNTRY.get(locale.getLanguage());
      if (country == null) {
        return CurrencyCode.USD;
      }
      resolved = Locale.of(locale.getLanguage(), country);
    }

    try {
      Currency currency = Currency.getInstance(resolved);
      return CurrencyCode.valueOf(currency.getCurrencyCode());
    } catch (IllegalArgumentException e) {
      return CurrencyCode.USD;
    }
  }
}
