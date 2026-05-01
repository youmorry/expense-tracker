import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryRouter, RouterProvider } from "react-router";
import { describe, expect, it, vi } from "vitest";

import { routes } from "../../../routes";

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

describe("SettingsPage", () => {
  it("renders user email and display name when user is loaded", async () => {
    renderSettingsPage();

    expect(await screen.findByText("test@example.com")).toBeInTheDocument();
    expect(screen.getByText("Test User")).toBeInTheDocument();
  });

  it("clears token and redirects to login when log out is clicked", async () => {
    const user = userEvent.setup();
    mockClearToken.mockClear();

    renderSettingsPage();
    await screen.findByText("test@example.com");

    await user.click(screen.getByRole("button", { name: /log out/i }));

    await waitFor(() => {
      expect(mockClearToken).toHaveBeenCalledTimes(1);
    });
  });
});
