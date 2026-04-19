import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import type { ReactNode } from "react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { describe, expect, it, vi } from "vitest";

import { routes } from "../../../routes";

vi.mock("../../../lib/auth", () => ({
  getToken: vi.fn(),
  setToken: vi.fn(),
  clearToken: vi.fn(),
}));

const { getToken } = await import("../../../lib/auth");
const mockGetToken = vi.mocked(getToken);

vi.mock("@react-oauth/google", () => ({
  GoogleLogin: () => (
    <button data-testid="google-login-button" type="button">
      Sign in with Google
    </button>
  ),
  GoogleOAuthProvider: ({ children }: { children: ReactNode }) => <>{children}</>,
}));

function renderLoginPage() {
  const queryClient = new QueryClient({
    defaultOptions: { mutations: { retry: false } },
  });
  const router = createMemoryRouter(routes, { initialEntries: ["/login"] });
  return render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  );
}

describe("LoginPage", () => {
  it("renders app name", () => {
    mockGetToken.mockReturnValue(null);

    renderLoginPage();

    expect(screen.getByText("Expense Tracker")).toBeInTheDocument();
  });

  it("renders Google login button", () => {
    mockGetToken.mockReturnValue(null);

    renderLoginPage();

    expect(screen.getByTestId("google-login-button")).toBeInTheDocument();
  });

  it("redirects to /transactions when already authenticated", () => {
    mockGetToken.mockReturnValue("existing-jwt-token");

    renderLoginPage();

    expect(screen.queryByText("Expense Tracker")).not.toBeInTheDocument();
  });
});
