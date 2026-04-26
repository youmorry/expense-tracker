import { useCallback, useMemo, useSyncExternalStore } from "react";

import {
  detectCurrencyFromLocale,
  getCurrencyDecimalDigits,
  getCurrencyFormatter,
} from "@/lib/currency";

const STORAGE_KEY = "expense-tracker:currency";

const listeners = new Set<() => void>();

function notify(): void {
  listeners.forEach((listener) => {
    listener();
  });
}

if (typeof window !== "undefined") {
  window.addEventListener("storage", (event) => {
    if (event.key === STORAGE_KEY) notify();
  });
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

function getSnapshot(): string {
  return localStorage.getItem(STORAGE_KEY) ?? detectCurrencyFromLocale(navigator.language);
}

function getServerSnapshot(): string {
  return "USD";
}

interface UseCurrencyReturn {
  currency: string;
  setCurrency: (code: string) => void;
  formatAmount: (amount: number) => string;
  decimalDigits: number;
}

export function useCurrency(): UseCurrencyReturn {
  const currency = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);

  const setCurrency = useCallback((code: string) => {
    localStorage.setItem(STORAGE_KEY, code);
    notify();
  }, []);

  const formatter = useMemo(() => getCurrencyFormatter(currency), [currency]);
  const formatAmount = useCallback((amount: number) => formatter.format(amount), [formatter]);
  const decimalDigits = useMemo(() => getCurrencyDecimalDigits(currency), [currency]);

  return { currency, setCurrency, formatAmount, decimalDigits };
}
