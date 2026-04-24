/**
 * 通貨コード（ISO 4217）に関する純粋ユーティリティ。
 *
 * 通貨設定はフロントエンドで管理する（バックエンドは金額の数値のみ扱う）。
 * @see docs/03-design/backend/domain-model.md
 */

const REGION_TO_CURRENCY: Record<string, string> = {
  JP: "JPY",
  US: "USD",
  GB: "GBP",
  AU: "AUD",
  CA: "CAD",
  KR: "KRW",
  CN: "CNY",
  TW: "TWD",
  HK: "HKD",
  SG: "SGD",
  CH: "CHF",
  SE: "SEK",
  NO: "NOK",
  DK: "DKK",
  NZ: "NZD",
  IN: "INR",
  BR: "BRL",
  MX: "MXN",
  // ユーロ圏
  DE: "EUR",
  FR: "EUR",
  IT: "EUR",
  ES: "EUR",
  NL: "EUR",
  BE: "EUR",
  AT: "EUR",
  IE: "EUR",
  PT: "EUR",
  FI: "EUR",
  GR: "EUR",
};

const FALLBACK_CURRENCY = "USD";

export function detectCurrencyFromLocale(locale: string): string {
  try {
    const region = new Intl.Locale(locale).maximize().region;
    if (!region) {
      return FALLBACK_CURRENCY;
    }
    return REGION_TO_CURRENCY[region] ?? FALLBACK_CURRENCY;
  } catch {
    return FALLBACK_CURRENCY;
  }
}

export function getCurrencyDecimalDigits(currency: string): number {
  return new Intl.NumberFormat(undefined, {
    style: "currency",
    currency,
  }).resolvedOptions().maximumFractionDigits;
}

export function formatCurrency(amount: number, currency: string, locale?: string): string {
  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency,
  }).format(amount);
}
