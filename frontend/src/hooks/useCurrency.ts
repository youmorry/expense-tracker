import { useCallback, useMemo, useSyncExternalStore } from "react";

import { detectCurrencyFromLocale, formatCurrency, getCurrencyDecimalDigits } from "@/lib/currency";

const STORAGE_KEY = "expense-tracker:currency";

const listeners = new Set<() => void>();

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  const handleStorage = (event: StorageEvent) => {
    if (event.key === STORAGE_KEY) {
      listener();
    }
  };
  window.addEventListener("storage", handleStorage);
  return () => {
    listeners.delete(listener);
    window.removeEventListener("storage", handleStorage);
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
    listeners.forEach((listener) => {
      listener();
    });
  }, []);

  const formatAmount = useCallback(
    (amount: number) => formatCurrency(amount, currency),
    [currency],
  );

  const decimalDigits = useMemo(() => getCurrencyDecimalDigits(currency), [currency]);

  return { currency, setCurrency, formatAmount, decimalDigits };
}
