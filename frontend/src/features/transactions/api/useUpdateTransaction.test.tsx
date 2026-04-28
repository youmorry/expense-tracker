import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { describe, expect, it } from "vitest";

import { server } from "../../../test/mocks/server";
import { useTransactions } from "./useTransactions";
import { useUpdateTransaction } from "./useUpdateTransaction";

interface WrapperContext {
  queryClient: QueryClient;
  Wrapper: (props: { children: ReactNode }) => ReactNode;
}

function createWrapper(): WrapperContext {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, staleTime: 0 },
      mutations: { retry: false },
    },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return { queryClient, Wrapper };
}

describe("useUpdateTransaction", () => {
  it("returns the updated transaction when API responds with 200", async () => {
    server.use(
      http.put("/api/v1/transactions/:id", () => {
        return HttpResponse.json({
          id: 42,
          date: "2026-04-27",
          amount: "2000",
          category_id: 1,
          category_name: "Food",
          need_want_type: "NEED",
          title: "Dinner",
          created_at: "2026-04-27T12:00:00Z",
          updated_at: "2026-04-27T12:00:00Z",
        });
      }),
    );

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useUpdateTransaction(42), { wrapper: Wrapper });

    result.current.mutate({
      date: "2026-04-27",
      amount: "2000",
      categoryId: 1,
      needWantType: "NEED",
      title: "Dinner",
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(result.current.data).toEqual(
      expect.objectContaining({
        id: 42,
        categoryId: 1,
        categoryName: "Food",
        needWantType: "NEED",
        title: "Dinner",
      }),
    );
  });

  it("sends the request body with snake_case keys", async () => {
    let capturedBody: unknown;
    server.use(
      http.put("/api/v1/transactions/:id", async ({ request }) => {
        capturedBody = await request.json();
        return HttpResponse.json({
          id: 1,
          date: "2026-04-27",
          amount: "1500",
          category_id: 2,
          category_name: "Transport",
          need_want_type: "WANT",
          created_at: "2026-04-27T12:00:00Z",
          updated_at: "2026-04-27T12:00:00Z",
        });
      }),
    );

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useUpdateTransaction(1), { wrapper: Wrapper });

    result.current.mutate({
      date: "2026-04-27",
      amount: "1500",
      categoryId: 2,
      needWantType: "WANT",
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedBody).toEqual({
      date: "2026-04-27",
      amount: "1500",
      category_id: 2,
      need_want_type: "WANT",
    });
  });

  it("invalidates the transactions query after a successful update", async () => {
    let getCallCount = 0;
    server.use(
      http.get("/api/v1/transactions", () => {
        getCallCount += 1;
        return HttpResponse.json({ items: [] });
      }),
      http.put("/api/v1/transactions/:id", () => {
        return HttpResponse.json({
          id: 1,
          date: "2026-04-27",
          amount: "1000",
          category_id: 11,
          category_name: "Uncategorized",
          need_want_type: "UNSET",
          created_at: "2026-04-27T12:00:00Z",
          updated_at: "2026-04-27T12:00:00Z",
        });
      }),
    );

    const { Wrapper } = createWrapper();
    const { result } = renderHook(
      () => ({
        list: useTransactions({}),
        update: useUpdateTransaction(1),
      }),
      { wrapper: Wrapper },
    );

    await waitFor(() => {
      expect(result.current.list.isSuccess).toBe(true);
    });
    expect(getCallCount).toBe(1);

    result.current.update.mutate({ date: "2026-04-27", amount: "1000" });

    await waitFor(() => {
      expect(result.current.update.isSuccess).toBe(true);
    });
    await waitFor(() => {
      expect(getCallCount).toBe(2);
    });
  });
});
