import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { describe, expect, it } from "vitest";

import { toIsoDate } from "../../../lib/isoDate";
import { server } from "../../../test/mocks/server";
import { useTransactions } from "./useTransactions";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useTransactions", () => {
  it("returns transactions when API responds successfully", async () => {
    server.use(
      http.get("/api/v1/transactions", () => {
        return HttpResponse.json({
          items: [
            {
              id: 1,
              date: "2026-02-23",
              amount: "1200",
              category_id: 1,
              category_name: "Food",
              need_want_type: "NEED",
              title: "Lunch",
              created_at: "2026-02-23T10:30:00Z",
              updated_at: "2026-02-23T10:30:00Z",
            },
          ],
        });
      }),
    );

    const { result } = renderHook(() => useTransactions({}), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(result.current.data?.items).toHaveLength(1);
    expect(result.current.data?.items[0]).toMatchObject({
      id: 1,
      categoryName: "Food",
      needWantType: "NEED",
      title: "Lunch",
    });
  });

  it("sends from and to as query parameters when provided", async () => {
    let capturedUrl: URL | null = null;
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        capturedUrl = new URL(request.url);
        return HttpResponse.json({ items: [] });
      }),
    );

    const { result } = renderHook(
      () =>
        useTransactions({
          from: toIsoDate(2026, 2, 1),
          to: toIsoDate(2026, 2, 28),
        }),
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedUrl).not.toBeNull();
    expect(capturedUrl?.searchParams.get("from")).toBe("2026-02-01");
    expect(capturedUrl?.searchParams.get("to")).toBe("2026-02-28");
  });

  it("omits from and to from the query string when not provided", async () => {
    let capturedUrl: URL | null = null;
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        capturedUrl = new URL(request.url);
        return HttpResponse.json({ items: [] });
      }),
    );

    const { result } = renderHook(() => useTransactions({}), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedUrl?.searchParams.has("from")).toBe(false);
    expect(capturedUrl?.searchParams.has("to")).toBe(false);
  });

  it("uses distinct cache keys for different period parameters", async () => {
    const requestedQueries: string[] = [];
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        requestedQueries.push(new URL(request.url).search);
        return HttpResponse.json({ items: [] });
      }),
    );

    const wrapper = createWrapper();
    const { rerender, result } = renderHook(
      ({ from, to }: { from?: ReturnType<typeof toIsoDate>; to?: ReturnType<typeof toIsoDate> }) =>
        useTransactions({ from, to }),
      {
        wrapper,
        initialProps: { from: toIsoDate(2026, 2, 1), to: toIsoDate(2026, 2, 28) },
      },
    );

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    rerender({ from: toIsoDate(2026, 3, 1), to: toIsoDate(2026, 3, 31) });

    await waitFor(() => {
      expect(requestedQueries).toHaveLength(2);
    });
    expect(requestedQueries[0]).toContain("from=2026-02-01");
    expect(requestedQueries[1]).toContain("from=2026-03-01");
  });
});
