import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";

import { ApiException } from "../../../lib/api/errors";
import { server } from "../../../test/mocks/server";
import { useGoogleLogin } from "./useGoogleLogin";

const mockNavigate = vi.fn();
vi.mock("react-router", () => ({
  useNavigate: () => mockNavigate,
}));

const mockSetToken = vi.fn();
vi.mock("../../../lib/auth", () => ({
  setToken: (token: string): void => {
    mockSetToken(token);
  },
  clearToken: vi.fn(),
  getToken: vi.fn(),
}));

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { mutations: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useGoogleLogin", () => {
  it("returns accessToken and navigates to /transactions on success", async () => {
    server.use(
      http.post("/api/v1/auth/google", () => {
        return HttpResponse.json({
          access_token: "jwt-token-123",
          user: {
            id: 1,
            email: "test@example.com",
            display_name: "Test User",
            created_at: "2026-01-01T00:00:00Z",
          },
        });
      }),
    );

    const { result } = renderHook(() => useGoogleLogin(), { wrapper: createWrapper() });

    result.current.mutate("google-id-token");

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(mockSetToken).toHaveBeenCalledWith("jwt-token-123");
    expect(mockNavigate).toHaveBeenCalledWith("/transactions", { replace: true });
  });

  it("sends idToken as id_token in request body", async () => {
    let capturedBody: unknown;
    server.use(
      http.post("/api/v1/auth/google", async ({ request }) => {
        capturedBody = await request.json();
        return HttpResponse.json({
          access_token: "jwt-token-123",
          user: {
            id: 1,
            email: "test@example.com",
            display_name: "Test User",
            created_at: "2026-01-01T00:00:00Z",
          },
        });
      }),
    );

    const { result } = renderHook(() => useGoogleLogin(), { wrapper: createWrapper() });

    result.current.mutate("google-id-token");

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(capturedBody).toEqual({ id_token: "google-id-token" });
  });

  it("throws ApiException when API returns error", async () => {
    server.use(
      http.post("/api/v1/auth/google", () => {
        return HttpResponse.json(
          {
            type: "about:blank",
            title: "Unauthorized",
            status: 401,
            detail: "Invalid ID token",
          },
          { status: 401 },
        );
      }),
    );

    const { result } = renderHook(() => useGoogleLogin(), { wrapper: createWrapper() });

    result.current.mutate("invalid-token");

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });
    expect(result.current.error).toBeInstanceOf(ApiException);
    expect(mockSetToken).not.toHaveBeenCalled();
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
