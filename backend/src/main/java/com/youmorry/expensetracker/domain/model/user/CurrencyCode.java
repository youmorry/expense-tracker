package com.youmorry.expensetracker.domain.model.user;

import java.util.Currency;
import java.util.Objects;

/** ISO 4217 通貨コードを表す値オブジェクト。 */
public record CurrencyCode(String value) {

  /** 通貨コードが ISO 4217 に準拠していることを検証する。 */
  public CurrencyCode {
    Objects.requireNonNull(value, "Currency code must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("Currency code must not be blank");
    }
    try {
      Currency.getInstance(value);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid ISO 4217 currency code: " + value);
    }
  }
}
