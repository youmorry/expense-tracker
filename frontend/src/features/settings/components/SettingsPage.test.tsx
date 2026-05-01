import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { describe, expect, it, vi } from "vitest";

import { routes } from "../../../routes";
import { server } from "../../../test/mocks/server";

vi.mock("../../../lib/auth", () => ({
  getToken: vi.fn(() => "existing-token"),
  setToken: vi.fn(),
  clearToken: vi.fn(),
}));

const { clearToken } = await import("../../../lib/auth");
const mockClearToken = vi.mocked(clearToken);

function renderSettingsPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  const router = createMemoryRouter(routes, { initialEntries: ["/settings"] });
  return render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  );
}

function mockUserResponse(): void {
  server.use(
    http.get("/api/v1/users/me", () => {
      return HttpResponse.json({
        id: 1,
        email: "user@example.com",
        display_name: "Example User",
        created_at: "2026-01-01T00:00:00Z",
      });
    }),
  );
}

describe("SettingsPage", () => {
  it("renders page heading", () => {
    mockUserResponse();

    renderSettingsPage();

    expect(screen.getByRole("heading", { level: 1, name: /settings/i })).toBeInTheDocument();
  });

  it("renders user email and display name when user is loaded", async () => {
    mockUserResponse();

    renderSettingsPage();

    expect(await screen.findByText("user@example.com")).toBeInTheDocument();
    expect(screen.getByText("Example User")).toBeInTheDocument();
  });

  it("clears token and redirects to login when log out is clicked", async () => {
    mockUserResponse();
    const user = userEvent.setup();
    mockClearToken.mockClear();

    renderSettingsPage();
    await screen.findByText("user@example.com");

    await user.click(screen.getByRole("button", { name: /log out/i }));

    await waitFor(() => {
      expect(mockClearToken).toHaveBeenCalledTimes(1);
    });
  });
});
