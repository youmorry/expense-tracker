import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";

import { ApiException } from "../../../lib/api/errors";
import { server } from "../../../test/mocks/server";
import { useCategories } from "./useCategories";

vi.mock("../../../lib/auth", () => ({
  getToken: (): string | null => "test-token",
  setToken: vi.fn(),
  clearToken: vi.fn(),
}));

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useCategories", () => {
  it("returns categories with camelCase keys when API responds successfully", async () => {
    server.use(
      http.get("/api/v1/categories", () => {
        return HttpResponse.json({
          items: [
            { id: 1, name: "Food", display_order: 1 },
            { id: 11, name: "Uncategorized", display_order: 11 },
          ],
        });
      }),
    );

    const { result } = renderHook(() => useCategories(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(result.current.data).toEqual({
      items: [
        { id: 1, name: "Food", displayOrder: 1 },
        { id: 11, name: "Uncategorized", displayOrder: 11 },
      ],
    });
  });

  it("throws ApiException when API returns error", async () => {
    server.use(
      http.get("/api/v1/categories", () => {
        return HttpResponse.json(
          {
            type: "about:blank",
            title: "Unauthorized",
            status: 401,
            detail: "Missing token",
          },
          { status: 401 },
        );
      }),
    );

    const { result } = renderHook(() => useCategories(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });
    expect(result.current.error).toBeInstanceOf(ApiException);
  });
});
