import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { describe, expect, it } from "vitest";

import { server } from "../../../test/mocks/server";
import { useUser } from "./useUser";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useUser", () => {
  it("returns user when API responds successfully", async () => {
    server.use(
      http.get("/api/v1/users/me", () => {
        return HttpResponse.json({
          id: 42,
          email: "user@example.com",
          display_name: "Example User",
          created_at: "2026-01-01T00:00:00Z",
        });
      }),
    );

    const { result } = renderHook(() => useUser(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(result.current.data).toEqual({
      id: 42,
      email: "user@example.com",
      displayName: "Example User",
      createdAt: "2026-01-01T00:00:00Z",
    });
  });
});
