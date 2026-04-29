import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { describe, expect, it } from "vitest";

import { server } from "../../../test/mocks/server";
import { useDeleteTransaction } from "./useDeleteTransaction";
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

describe("useDeleteTransaction", () => {
  it("resolves when the API responds with 204", async () => {
    server.use(
      http.delete("/api/v1/transactions/:id", () => {
        return new HttpResponse(null, { status: 204 });
      }),
    );

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useDeleteTransaction(), { wrapper: Wrapper });

    result.current.mutate(42);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
  });

  it("sends a DELETE request to the path containing the given id", async () => {
    let capturedUrl = "";
    let capturedMethod = "";
    server.use(
      http.delete("/api/v1/transactions/:id", ({ request }) => {
        capturedUrl = request.url;
        capturedMethod = request.method;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useDeleteTransaction(), { wrapper: Wrapper });

    result.current.mutate(123);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedMethod).toBe("DELETE");
    expect(capturedUrl).toContain("/api/v1/transactions/123");
  });

  it("invalidates the transactions query after a successful delete", async () => {
    let getCallCount = 0;
    server.use(
      http.get("/api/v1/transactions", () => {
        getCallCount += 1;
        return HttpResponse.json({ items: [] });
      }),
      http.delete("/api/v1/transactions/:id", () => {
        return new HttpResponse(null, { status: 204 });
      }),
    );

    const { Wrapper } = createWrapper();
    const { result } = renderHook(
      () => ({
        list: useTransactions({}),
        delete: useDeleteTransaction(),
      }),
      { wrapper: Wrapper },
    );

    await waitFor(() => {
      expect(result.current.list.isSuccess).toBe(true);
    });
    expect(getCallCount).toBe(1);

    result.current.delete.mutate(1);

    await waitFor(() => {
      expect(result.current.delete.isSuccess).toBe(true);
    });
    await waitFor(() => {
      expect(getCallCount).toBe(2);
    });
  });
});
