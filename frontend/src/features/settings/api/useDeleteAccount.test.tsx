import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { describe, expect, it } from "vitest";

import { server } from "../../../test/mocks/server";
import { useDeleteAccount } from "./useDeleteAccount";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, staleTime: 0 },
      mutations: { retry: false },
    },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useDeleteAccount", () => {
  it("resolves when the API responds with 204", async () => {
    server.use(
      http.delete("/api/v1/users/me", () => {
        return new HttpResponse(null, { status: 204 });
      }),
    );

    const { result } = renderHook(() => useDeleteAccount(), { wrapper: createWrapper() });

    result.current.mutate();

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
  });

  it("sends a DELETE request to /api/v1/users/me", async () => {
    let capturedUrl = "";
    let capturedMethod = "";
    server.use(
      http.delete("/api/v1/users/me", ({ request }) => {
        capturedUrl = request.url;
        capturedMethod = request.method;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    const { result } = renderHook(() => useDeleteAccount(), { wrapper: createWrapper() });

    result.current.mutate();

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedMethod).toBe("DELETE");
    expect(capturedUrl).toContain("/api/v1/users/me");
  });
});
