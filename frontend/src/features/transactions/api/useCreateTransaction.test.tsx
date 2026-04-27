import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";

import { server } from "../../../test/mocks/server";
import { useCreateTransaction } from "./useCreateTransaction";
import { useTransactions } from "./useTransactions";

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

describe("useCreateTransaction", () => {
  it("returns the created transaction when API responds with 201", async () => {
    server.use(
      http.post("/api/v1/transactions", () => {
        return HttpResponse.json(
          {
            id: 42,
            date: "2026-04-27",
            amount: "1500",
            category_id: 1,
            category_name: "Food",
            need_want_type: "NEED",
            title: "Lunch",
            created_at: "2026-04-27T12:00:00Z",
            updated_at: "2026-04-27T12:00:00Z",
          },
          { status: 201 },
        );
      }),
    );

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useCreateTransaction(), { wrapper: Wrapper });

    result.current.mutate({
      date: "2026-04-27",
      amount: "1500",
      categoryId: 1,
      needWantType: "NEED",
      title: "Lunch",
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
        title: "Lunch",
      }),
    );
  });

  it("sends the request body with snake_case keys", async () => {
    let capturedBody: unknown;
    server.use(
      http.post("/api/v1/transactions", async ({ request }) => {
        capturedBody = await request.json();
        return HttpResponse.json(
          {
            id: 1,
            date: "2026-04-27",
            amount: "1500",
            category_id: 2,
            category_name: "Transport",
            need_want_type: "WANT",
            created_at: "2026-04-27T12:00:00Z",
            updated_at: "2026-04-27T12:00:00Z",
          },
          { status: 201 },
        );
      }),
    );

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useCreateTransaction(), { wrapper: Wrapper });

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

  it("invalidates the transactions query after a successful create", async () => {
    let getCallCount = 0;
    server.use(
      http.get("/api/v1/transactions", () => {
        getCallCount += 1;
        return HttpResponse.json({ items: [] });
      }),
      http.post("/api/v1/transactions", () => {
        return HttpResponse.json(
          {
            id: 1,
            date: "2026-04-27",
            amount: "1000",
            category_id: 11,
            category_name: "Uncategorized",
            need_want_type: "UNSET",
            created_at: "2026-04-27T12:00:00Z",
            updated_at: "2026-04-27T12:00:00Z",
          },
          { status: 201 },
        );
      }),
    );

    const { Wrapper } = createWrapper();
    const { result } = renderHook(
      () => ({
        list: useTransactions({}),
        create: useCreateTransaction(),
      }),
      { wrapper: Wrapper },
    );

    await waitFor(() => {
      expect(result.current.list.isSuccess).toBe(true);
    });
    expect(getCallCount).toBe(1);

    result.current.create.mutate({ date: "2026-04-27", amount: "1000" });

    await waitFor(() => {
      expect(result.current.create.isSuccess).toBe(true);
    });
    await waitFor(() => {
      expect(getCallCount).toBe(2);
    });
  });

  it("does not invalidate the transactions query when the create fails", async () => {
    server.use(
      http.post("/api/v1/transactions", () => {
        return HttpResponse.json(
          {
            type: "about:blank",
            title: "Validation Error",
            status: 422,
            detail: "Invalid input",
          },
          { status: 422 },
        );
      }),
    );

    const { queryClient, Wrapper } = createWrapper();
    const invalidateSpy = vi.spyOn(queryClient, "invalidateQueries");
    const { result } = renderHook(() => useCreateTransaction(), { wrapper: Wrapper });

    result.current.mutate({ date: "2026-04-27", amount: "1000" });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });
    expect(invalidateSpy).not.toHaveBeenCalled();
  });
});
