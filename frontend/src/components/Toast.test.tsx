import { act, renderHook, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { ToastProvider } from "@/components/Toast";
import { useToast } from "@/hooks/useToast";

function renderToastHook() {
  return renderHook(() => useToast(), { wrapper: ToastProvider });
}

describe("Toast", () => {
  it("renders success toast with the provided message", () => {
    const { result } = renderToastHook();

    act(() => {
      result.current.showSuccess("Transaction saved");
    });

    expect(screen.getByText("Transaction saved")).toBeInTheDocument();
  });

  it("renders error toast with the provided message", () => {
    const { result } = renderToastHook();

    act(() => {
      result.current.showError("Something went wrong. Please try again.");
    });

    expect(screen.getByText("Something went wrong. Please try again.")).toBeInTheDocument();
  });

  it("renders a close button for error variant", () => {
    const { result } = renderToastHook();

    act(() => {
      result.current.showError("Error message");
    });

    expect(screen.getByRole("button", { name: /close/i })).toBeInTheDocument();
  });

  it("does not render a close button for success variant", () => {
    const { result } = renderToastHook();

    act(() => {
      result.current.showSuccess("Success message");
    });

    expect(screen.queryByRole("button", { name: /close/i })).not.toBeInTheDocument();
  });

  it("dismisses error toast when close button is clicked", async () => {
    const user = userEvent.setup();
    const { result } = renderToastHook();

    act(() => {
      result.current.showError("Dismissable error");
    });

    await user.click(screen.getByRole("button", { name: /close/i }));

    expect(screen.queryByText("Dismissable error")).not.toBeInTheDocument();
  });

  describe("auto-dismiss", () => {
    beforeEach(() => {
      vi.useFakeTimers();
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it("auto-dismisses success toast after 3 seconds", () => {
      const { result } = renderToastHook();

      act(() => {
        result.current.showSuccess("Auto dismiss me");
      });
      expect(screen.getByText("Auto dismiss me")).toBeInTheDocument();

      act(() => {
        vi.advanceTimersByTime(3000);
      });

      expect(screen.queryByText("Auto dismiss me")).not.toBeInTheDocument();
    });

    it("keeps error toast visible after 3 seconds", () => {
      const { result } = renderToastHook();

      act(() => {
        result.current.showError("Sticky error");
      });

      act(() => {
        vi.advanceTimersByTime(10_000);
      });

      expect(screen.getByText("Sticky error")).toBeInTheDocument();
    });
  });
});
