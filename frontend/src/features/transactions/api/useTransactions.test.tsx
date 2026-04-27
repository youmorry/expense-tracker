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
    expect(result.current.data).toEqual({
      items: [
        expect.objectContaining({
          id: 1,
          categoryName: "Food",
          needWantType: "NEED",
          title: "Lunch",
        }),
      ],
    });
  });

  it("sends from and to as query parameters when provided", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        capturedUrl = request.url;
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
    expect(capturedUrl).toContain("from=2026-02-01");
    expect(capturedUrl).toContain("to=2026-02-28");
  });

  it("omits from and to from the query string when not provided", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ items: [] });
      }),
    );

    const { result } = renderHook(() => useTransactions({}), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedUrl).not.toContain("from=");
    expect(capturedUrl).not.toContain("to=");
  });

  it("sends keyword as a query parameter when provided", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ items: [] });
      }),
    );

    const { result } = renderHook(() => useTransactions({ keyword: "lunch" }), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedUrl).toContain("keyword=lunch");
  });

  it("sends each categoryId as a repeated category_id query parameter", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ items: [] });
      }),
    );

    const { result } = renderHook(() => useTransactions({ categoryIds: [1, 3] }), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    const url = new URL(capturedUrl);
    expect(url.searchParams.getAll("category_id")).toEqual(["1", "3"]);
  });

  it("sends needWantType as the need_want_type query parameter when provided", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ items: [] });
      }),
    );

    const { result } = renderHook(() => useTransactions({ needWantType: "WANT" }), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedUrl).toContain("need_want_type=WANT");
  });

  it("omits keyword when it is an empty string", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ items: [] });
      }),
    );

    const { result } = renderHook(() => useTransactions({ keyword: "" }), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedUrl).not.toContain("keyword=");
  });

  it("omits categoryIds from the query when empty", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ items: [] });
      }),
    );

    const { result } = renderHook(() => useTransactions({ categoryIds: [] }), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedUrl).not.toContain("category_id=");
  });

  it("uses distinct cache keys for different period parameters", async () => {
    const requestedUrls: string[] = [];
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        requestedUrls.push(request.url);
        return HttpResponse.json({ items: [] });
      }),
    );

    const wrapper = createWrapper();
    const { rerender, result } = renderHook(
      (params: { from: ReturnType<typeof toIsoDate>; to: ReturnType<typeof toIsoDate> }) =>
        useTransactions(params),
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
