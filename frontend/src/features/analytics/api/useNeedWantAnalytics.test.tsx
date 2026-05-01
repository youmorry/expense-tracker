import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { describe, expect, it } from "vitest";

import { toIsoDate } from "../../../lib/isoDate";
import { server } from "../../../test/mocks/server";
import { useNeedWantAnalytics } from "./useNeedWantAnalytics";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useNeedWantAnalytics", () => {
  it("returns the parsed analytics response when the API responds successfully", async () => {
    server.use(
      http.get("/api/v1/analytics/need-want", () => {
        return HttpResponse.json({
          total_amount: "130000",
          breakdown: [
            { type: "NEED", amount: "80000", percentage: 61.5, transaction_count: 20 },
            { type: "WANT", amount: "35000", percentage: 26.9, transaction_count: 10 },
            { type: "UNSET", amount: "15000", percentage: 11.5, transaction_count: 3 },
          ],
        });
      }),
    );

    const { result } = renderHook(
      () => useNeedWantAnalytics({ from: toIsoDate(2026, 2, 1), to: toIsoDate(2026, 2, 28) }),
      { wrapper: createWrapper() },
    );

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(result.current.data).toEqual({
      totalAmount: "130000",
      breakdown: [
        expect.objectContaining({
          type: "NEED",
          amount: "80000",
          percentage: 61.5,
          transactionCount: 20,
        }),
        expect.objectContaining({ type: "WANT", transactionCount: 10 }),
        expect.objectContaining({ type: "UNSET", transactionCount: 3 }),
      ],
    });
  });

  it("sends from and to as query parameters when a period is provided", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/analytics/need-want", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ total_amount: "0", breakdown: [] });
      }),
    );

    const { result } = renderHook(
      () => useNeedWantAnalytics({ from: toIsoDate(2026, 2, 1), to: toIsoDate(2026, 2, 28) }),
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
      http.get("/api/v1/analytics/need-want", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ total_amount: "0", breakdown: [] });
      }),
    );

    const { result } = renderHook(() => useNeedWantAnalytics(null), {
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
      http.get("/api/v1/analytics/need-want", ({ request }) => {
        requestedUrls.push(request.url);
        return HttpResponse.json({ total_amount: "0", breakdown: [] });
      }),
    );

    const wrapper = createWrapper();
    const { rerender, result } = renderHook(
      (period: { from: ReturnType<typeof toIsoDate>; to: ReturnType<typeof toIsoDate> }) =>
        useNeedWantAnalytics(period),
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
