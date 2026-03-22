package com.youmorry.expensetracker.domain.model.user;

import java.util.Currency;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/** {@link Locale} から ISO 4217 通貨コードを推定する。 */
public final class LocaleCurrencyMapper {

  private LocaleCurrencyMapper() {}

  /**
   * 指定された {@link Locale} を ISO 4217 通貨コードに変換する。
   *
   * <p>{@link Currency#getInstance(Locale)} を使用して JDK の locale-通貨マッピングで解決する。 解決できない場合は USD を返す。
   *
   * @param locale ロケール（例: {@code Locale.of("ja", "JP")}）。null 可
   * @return 推定された通貨コード
   */
  public static CurrencyCode toCurrencyCode(@Nullable Locale locale) {
    if (locale == null || locale.getCountry().isEmpty()) {
      return CurrencyCode.USD;
    }

    try {
      Currency currency = Currency.getInstance(locale);
      return new CurrencyCode(currency.getCurrencyCode());
    } catch (IllegalArgumentException e) {
      return CurrencyCode.USD;
    }
  }
}
