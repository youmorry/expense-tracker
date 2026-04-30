import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { describe, expect, it } from "vitest";

import { toIsoDate } from "../../../lib/isoDate";
import { server } from "../../../test/mocks/server";
import { useCategoryAnalytics } from "./useCategoryAnalytics";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useCategoryAnalytics", () => {
  it("returns the parsed analytics response when the API responds successfully", async () => {
    server.use(
      http.get("/api/v1/analytics/category", () => {
        return HttpResponse.json({
          total_amount: "12000",
          categories: [
            {
              category_id: 1,
              category_name: "Food",
              amount: "8000",
              percentage: 66.7,
              transaction_count: 4,
            },
            {
              category_id: 2,
              category_name: "Transport",
              amount: "4000",
              percentage: 33.3,
              transaction_count: 2,
            },
          ],
        });
      }),
    );

    const { result } = renderHook(
      () => useCategoryAnalytics({ from: toIsoDate(2026, 2, 1), to: toIsoDate(2026, 2, 28) }),
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(result.current.data).toEqual({
      totalAmount: "12000",
      categories: [
        expect.objectContaining({
          categoryId: 1,
          categoryName: "Food",
          amount: "8000",
          percentage: 66.7,
          transactionCount: 4,
        }),
        expect.objectContaining({
          categoryId: 2,
          categoryName: "Transport",
        }),
      ],
    });
  });

  it("sends from and to as query parameters when a period is provided", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/analytics/category", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ total_amount: "0", categories: [] });
      }),
    );

    const { result } = renderHook(
      () => useCategoryAnalytics({ from: toIsoDate(2026, 2, 1), to: toIsoDate(2026, 2, 28) }),
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedUrl).toContain("from=2026-02-01");
    expect(capturedUrl).toContain("to=2026-02-28");
  });

  it("omits from and to from the query string when the period is null", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/analytics/category", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ total_amount: "0", categories: [] });
      }),
    );

    const { result } = renderHook(() => useCategoryAnalytics(null), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedUrl).not.toContain("from=");
    expect(capturedUrl).not.toContain("to=");
  });

  it("uses distinct cache keys for different periods", async () => {
    const requestedUrls: string[] = [];
    server.use(
      http.get("/api/v1/analytics/category", ({ request }) => {
        requestedUrls.push(request.url);
        return HttpResponse.json({ total_amount: "0", categories: [] });
      }),
    );

    const wrapper = createWrapper();
    const { rerender, result } = renderHook(
      (period: { from: ReturnType<typeof toIsoDate>; to: ReturnType<typeof toIsoDate> }) =>
        useCategoryAnalytics(period),
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
      expect(requestedUrls).toHaveLength(2);
    });
    expect(requestedUrls[0]).toContain("from=2026-02-01");
    expect(requestedUrls[1]).toContain("from=2026-03-01");
  });
});
