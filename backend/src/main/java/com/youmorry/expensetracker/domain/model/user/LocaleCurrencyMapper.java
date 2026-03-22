package com.youmorry.expensetracker.domain.model.user;

import java.util.Currency;
import java.util.Locale;

/** BCP 47 locale 文字列から ISO 4217 通貨コードを推定する。 */
public final class LocaleCurrencyMapper {

  private LocaleCurrencyMapper() {}

  /**
   * 指定された BCP 47 locale 文字列を ISO 4217 通貨コードに変換する。
   *
   * <p>{@link Locale#forLanguageTag(String)} と {@link Currency#getInstance(Locale)} を使用して JDK の
   * locale-通貨マッピングで解決する。解決できない場合は USD を返す。
   *
   * @param locale BCP 47 locale 文字列（例: "ja-JP", "en-US"）。null 可
   * @return 推定された通貨コード
   */
  public static CurrencyCode toCurrencyCode(String locale) {
    if (locale == null || locale.isBlank()) {
      return CurrencyCode.USD;
    }

    try {
      Locale parsed = Locale.forLanguageTag(locale);
      if (parsed.getCountry().isEmpty()) {
        return CurrencyCode.USD;
      }
      Currency currency = Currency.getInstance(parsed);
      return new CurrencyCode(currency.getCurrencyCode());
    } catch (IllegalArgumentException e) {
      return CurrencyCode.USD;
    }
  }
}
