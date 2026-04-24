import { act, renderHook } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { useCurrency } from "@/hooks/useCurrency";

const STORAGE_KEY = "expense-tracker:currency";

describe("useCurrency", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("returns currency detected from navigator.language when localStorage is empty", () => {
    vi.spyOn(navigator, "language", "get").mockReturnValue("ja-JP");

    const { result } = renderHook(() => useCurrency());

    expect(result.current.currency).toBe("JPY");
  });

  it("returns persisted currency when localStorage has a value", () => {
    localStorage.setItem(STORAGE_KEY, "EUR");

    const { result } = renderHook(() => useCurrency());

    expect(result.current.currency).toBe("EUR");
  });

  it("persists currency to localStorage when setCurrency is called", () => {
    const { result } = renderHook(() => useCurrency());

    act(() => {
      result.current.setCurrency("GBP");
    });

    expect(localStorage.getItem(STORAGE_KEY)).toBe("GBP");
    expect(result.current.currency).toBe("GBP");
  });

  it("syncs currency across hook instances when setCurrency is called", () => {
    const { result: first } = renderHook(() => useCurrency());
    const { result: second } = renderHook(() => useCurrency());

    act(() => {
      first.current.setCurrency("GBP");
    });

    expect(second.current.currency).toBe("GBP");
  });

  it("formats amount as JPY without decimal places", () => {
    localStorage.setItem(STORAGE_KEY, "JPY");
    vi.spyOn(navigator, "language", "get").mockReturnValue("en-US");

    const { result } = renderHook(() => useCurrency());

    expect(result.current.formatAmount(1200)).toBe("¥1,200");
  });

  it("formats amount as USD with two decimal places", () => {
    localStorage.setItem(STORAGE_KEY, "USD");
    vi.spyOn(navigator, "language", "get").mockReturnValue("en-US");

    const { result } = renderHook(() => useCurrency());

    expect(result.current.formatAmount(12)).toBe("$12.00");
  });

  it("exposes 0 decimal digits for JPY", () => {
    localStorage.setItem(STORAGE_KEY, "JPY");

    const { result } = renderHook(() => useCurrency());

    expect(result.current.decimalDigits).toBe(0);
  });

  it("exposes 2 decimal digits for USD", () => {
    localStorage.setItem(STORAGE_KEY, "USD");

    const { result } = renderHook(() => useCurrency());

    expect(result.current.decimalDigits).toBe(2);
  });
});
