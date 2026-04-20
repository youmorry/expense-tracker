import { act, render, renderHook, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import { ToastProvider } from "@/components/Toast";
import { useToast } from "@/hooks/useToast";

describe("useToast", () => {
  it("throws error when used outside ToastProvider", () => {
    const consoleError = vi.spyOn(console, "error").mockImplementation(() => {});

    expect(() => renderHook(() => useToast())).toThrow(/ToastProvider/);

    consoleError.mockRestore();
  });

  it("exposes showSuccess and showError functions when used within ToastProvider", () => {
    const { result } = renderHook(() => useToast(), { wrapper: ToastProvider });

    expect(typeof result.current.showSuccess).toBe("function");
    expect(typeof result.current.showError).toBe("function");
  });

  it("renders success message when showSuccess is called", () => {
    const { result } = renderHook(() => useToast(), { wrapper: ToastProvider });

    act(() => {
      result.current.showSuccess("Transaction saved");
    });

    expect(screen.getByText("Transaction saved")).toBeInTheDocument();
  });

  it("renders error message when showError is called", () => {
    const { result } = renderHook(() => useToast(), { wrapper: ToastProvider });

    act(() => {
      result.current.showError("Something went wrong. Please try again.");
    });

    expect(screen.getByText("Something went wrong. Please try again.")).toBeInTheDocument();
  });
});
