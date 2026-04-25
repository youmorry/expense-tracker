import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import type { ReactNode } from "react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { routes } from "./routes";

vi.mock("./lib/auth", () => ({
  getToken: vi.fn(),
  setToken: vi.fn(),
  clearToken: vi.fn(),
}));

import { getToken } from "./lib/auth";

const mockedGetToken = vi.mocked(getToken);

vi.mock("@react-oauth/google", () => ({
  GoogleLogin: () => (
    <button data-testid="google-login-button" type="button">
      Sign in with Google
    </button>
  ),
  GoogleOAuthProvider: ({ children }: { children: ReactNode }) => <>{children}</>,
}));

function renderWithRouter(initialEntry: string) {
  const queryClient = new QueryClient({
    defaultOptions: { mutations: { retry: false } },
  });
  const router = createMemoryRouter(routes, { initialEntries: [initialEntry] });
  render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  );
  return router;
}

describe("routes", () => {
  beforeEach(() => {
    mockedGetToken.mockReturnValue(null);
  });

  it("redirects unauthenticated user from transactions to login", async () => {
    const router = renderWithRouter("/transactions");

    await screen.findByText("Expense Tracker");
    expect(router.state.location.pathname).toBe("/login");
  });

  it("redirects unauthenticated user from analytics to login", async () => {
    const router = renderWithRouter("/analytics");

    await screen.findByText("Expense Tracker");
    expect(router.state.location.pathname).toBe("/login");
  });

  it("redirects unauthenticated user from settings to login", async () => {
    const router = renderWithRouter("/settings");

    await screen.findByText("Expense Tracker");
    expect(router.state.location.pathname).toBe("/login");
  });

  it("returns transactions page when authenticated", async () => {
    mockedGetToken.mockReturnValue("valid-token");

    renderWithRouter("/transactions");

    await screen.findByRole("button", { name: /add transaction/i });
  });

  it("redirects root to transactions", async () => {
    mockedGetToken.mockReturnValue("valid-token");

    const router = renderWithRouter("/");

    await screen.findByRole("button", { name: /add transaction/i });
    expect(router.state.location.pathname).toBe("/transactions");
  });

  it("renders login page without authentication", async () => {
    renderWithRouter("/login");

    await screen.findByText("Expense Tracker");
  });
});
