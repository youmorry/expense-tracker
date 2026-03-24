package com.youmorry.expensetracker.domain.model.user;

import java.util.Currency;
import java.util.Objects;

/** ISO 4217 通貨コードを表す値オブジェクト。 */
public record CurrencyCode(String value) {

  public static final CurrencyCode JPY = new CurrencyCode("JPY");
  public static final CurrencyCode USD = new CurrencyCode("USD");
  public static final CurrencyCode EUR = new CurrencyCode("EUR");
  public static final CurrencyCode GBP = new CurrencyCode("GBP");
  public static final CurrencyCode AUD = new CurrencyCode("AUD");
  public static final CurrencyCode CAD = new CurrencyCode("CAD");
  public static final CurrencyCode BRL = new CurrencyCode("BRL");
  public static final CurrencyCode KRW = new CurrencyCode("KRW");
  public static final CurrencyCode CNY = new CurrencyCode("CNY");
  public static final CurrencyCode TWD = new CurrencyCode("TWD");
  public static final CurrencyCode THB = new CurrencyCode("THB");
  public static final CurrencyCode VND = new CurrencyCode("VND");
  public static final CurrencyCode IDR = new CurrencyCode("IDR");
  public static final CurrencyCode MYR = new CurrencyCode("MYR");
  public static final CurrencyCode INR = new CurrencyCode("INR");
  public static final CurrencyCode RUB = new CurrencyCode("RUB");
  public static final CurrencyCode TRY = new CurrencyCode("TRY");
  public static final CurrencyCode PLN = new CurrencyCode("PLN");
  public static final CurrencyCode SEK = new CurrencyCode("SEK");
  public static final CurrencyCode DKK = new CurrencyCode("DKK");
  public static final CurrencyCode NOK = new CurrencyCode("NOK");

  /** 通貨コードが ISO 4217 に準拠していることを検証する。 */
  public CurrencyCode {
    Objects.requireNonNull(value, "Currency code must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("Currency code must not be blank");
    }
    try {
      Currency.getInstance(value);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid ISO 4217 currency code: " + value, e);
    }
  }
}
