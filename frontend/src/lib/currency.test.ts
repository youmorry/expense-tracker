import { describe, expect, it } from "vitest";

import { detectCurrencyFromLocale, formatCurrency, getCurrencyDecimalDigits } from "./currency";

describe("detectCurrencyFromLocale", () => {
  it.each([
    ["ja-JP", "JPY"],
    ["en-US", "USD"],
    ["en-GB", "GBP"],
    ["de-DE", "EUR"],
    ["fr-FR", "EUR"],
    ["ko-KR", "KRW"],
    ["zh-CN", "CNY"],
  ])("returns %s currency for locale %s", (locale, expected) => {
    expect(detectCurrencyFromLocale(locale)).toBe(expected);
  });

  it("returns USD when locale region is unknown", () => {
    expect(detectCurrencyFromLocale("xx-ZZ")).toBe("USD");
  });

  it("returns USD when locale string is invalid", () => {
    expect(detectCurrencyFromLocale("not-a-locale!!")).toBe("USD");
  });

  it("returns USD when locale has no region subtag", () => {
    expect(detectCurrencyFromLocale("en")).toBe("USD");
  });
});

describe("getCurrencyDecimalDigits", () => {
  it("returns 0 for JPY", () => {
    expect(getCurrencyDecimalDigits("JPY")).toBe(0);
  });

  it("returns 2 for USD", () => {
    expect(getCurrencyDecimalDigits("USD")).toBe(2);
  });

  it("returns 2 for EUR", () => {
    expect(getCurrencyDecimalDigits("EUR")).toBe(2);
  });

  it("returns 0 for KRW", () => {
    expect(getCurrencyDecimalDigits("KRW")).toBe(0);
  });
});

describe("formatCurrency", () => {
  it("formats JPY without decimal places", () => {
    expect(formatCurrency(1200, "JPY", "en-US")).toBe("¥1,200");
  });

  it("formats USD with two decimal places", () => {
    expect(formatCurrency(12, "USD", "en-US")).toBe("$12.00");
  });

  it("formats USD rounding to two decimal places", () => {
    expect(formatCurrency(12.5, "USD", "en-US")).toBe("$12.50");
  });
});
