import { act, renderHook } from "@testing-library/react";
import type { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";

import { ToastProvider } from "../components/Toast";
import { ApiException, NetworkException } from "../lib/api/errors";
import { useApiError } from "./useApiError";

const showError = vi.fn();

vi.mock("./useToast", () => ({
  useToast: () => ({
    showError,
    showSuccess: vi.fn(),
  }),
}));

function wrapper({ children }: { children: ReactNode }) {
  return <ToastProvider>{children}</ToastProvider>;
}

function render() {
  showError.mockReset();
  return renderHook(() => useApiError(), { wrapper });
}

describe("useApiError", () => {
  describe("422 with errors[]", () => {
    it("populates fieldErrors keyed by camelCase field name", () => {
      const { result } = render();
      const error = new ApiException(422, {
        type: "/errors/validation-error",
        title: "Your request is not valid.",
        status: 422,
        detail: "One or more fields have validation errors.",
        errors: [
          { pointer: "#/amount", detail: "must be greater than 0" },
          { pointer: "#/category_id", detail: "category not found" },
        ],
      });

      act(() => {
        result.current.handleError(error);
      });

      expect(result.current.fieldErrors).toEqual({
        amount: "must be greater than 0",
        categoryId: "category not found",
      });
    });

    it("does not show a toast when field errors are present", () => {
      const { result } = render();
      const error = new ApiException(422, {
        type: "/errors/validation-error",
        title: "Your request is not valid.",
        status: 422,
        detail: "One or more fields have validation errors.",
        errors: [{ pointer: "#/amount", detail: "must be greater than 0" }],
      });

      act(() => {
        result.current.handleError(error);
      });

      expect(showError).not.toHaveBeenCalled();
    });
  });

  describe("422 without errors[]", () => {
    it("falls back to a toast with the detail message", () => {
      const { result } = render();
      const error = new ApiException(422, {
        type: "/errors/validation-error",
        title: "Your request is not valid.",
        status: 422,
        detail: "amount must be positive",
      });

      act(() => {
        result.current.handleError(error);
      });

      expect(showError).toHaveBeenCalledWith("amount must be positive");
      expect(result.current.fieldErrors).toEqual({});
    });
  });

  describe("non-422 ApiException", () => {
    it("shows a toast with the detail message", () => {
      const { result } = render();
      const error = new ApiException(404, {
        type: "about:blank",
        title: "Not Found",
        status: 404,
        detail: "transaction not found",
      });

      act(() => {
        result.current.handleError(error);
      });

      expect(showError).toHaveBeenCalledWith("transaction not found");
      expect(result.current.fieldErrors).toEqual({});
    });
  });

  describe("non-ApiException error", () => {
    it("shows a generic toast for NetworkException", () => {
      const { result } = render();

      act(() => {
        result.current.handleError(new NetworkException(new TypeError("fetch failed")));
      });

      expect(showError).toHaveBeenCalledWith("Network error. Please check your connection.");
    });

    it("shows a generic toast for unknown errors", () => {
      const { result } = render();

      act(() => {
        result.current.handleError(new Error("boom"));
      });

      expect(showError).toHaveBeenCalledWith("Something went wrong. Please try again.");
    });
  });

  describe("clearFieldError", () => {
    it("removes only the specified field from fieldErrors", () => {
      const { result } = render();
      act(() => {
        result.current.handleError(
          new ApiException(422, {
            type: "/errors/validation-error",
            title: "Your request is not valid.",
            status: 422,
            detail: "One or more fields have validation errors.",
            errors: [
              { pointer: "#/amount", detail: "must be greater than 0" },
              { pointer: "#/category_id", detail: "category not found" },
            ],
          }),
        );
      });

      act(() => {
        result.current.clearFieldError("amount");
      });

      expect(result.current.fieldErrors).toEqual({
        categoryId: "category not found",
      });
    });

    it("is a no-op when the field has no error", () => {
      const { result } = render();

      act(() => {
        result.current.clearFieldError("amount");
      });

      expect(result.current.fieldErrors).toEqual({});
    });
  });

  describe("clearAllFieldErrors", () => {
    it("removes all field errors", () => {
      const { result } = render();
      act(() => {
        result.current.handleError(
          new ApiException(422, {
            type: "/errors/validation-error",
            title: "Your request is not valid.",
            status: 422,
            detail: "One or more fields have validation errors.",
            errors: [{ pointer: "#/amount", detail: "must be greater than 0" }],
          }),
        );
      });

      act(() => {
        result.current.clearAllFieldErrors();
      });

      expect(result.current.fieldErrors).toEqual({});
    });
  });
});
