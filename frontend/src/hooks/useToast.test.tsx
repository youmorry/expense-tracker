import { renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import { useToast } from "@/hooks/useToast";

describe("useToast", () => {
  it("throws error when used outside ToastProvider", () => {
    const consoleError = vi.spyOn(console, "error").mockImplementation(() => {});

    expect(() => renderHook(() => useToast())).toThrow(/ToastProvider/);

    consoleError.mockRestore();
  });
});
