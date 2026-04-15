import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { routes } from "./routes";

vi.mock("./lib/auth", () => ({
  getToken: vi.fn(),
}));

import { getToken } from "./lib/auth";

const mockedGetToken = vi.mocked(getToken);

function renderWithRouter(initialEntry: string) {
  const router = createMemoryRouter(routes, { initialEntries: [initialEntry] });
  render(<RouterProvider router={router} />);
  return router;
}

describe("routes", () => {
  beforeEach(() => {
    mockedGetToken.mockReturnValue(null);
  });

  it("redirects_unauthenticated_user_from_transactions_to_login", async () => {
    const router = renderWithRouter("/transactions");

    await screen.findByRole("heading", { name: "Login" });
    expect(router.state.location.pathname).toBe("/login");
  });

  it("redirects_unauthenticated_user_from_analytics_to_login", async () => {
    const router = renderWithRouter("/analytics");

    await screen.findByRole("heading", { name: "Login" });
    expect(router.state.location.pathname).toBe("/login");
  });

  it("redirects_unauthenticated_user_from_settings_to_login", async () => {
    const router = renderWithRouter("/settings");

    await screen.findByRole("heading", { name: "Login" });
    expect(router.state.location.pathname).toBe("/login");
  });

  it("returns_transactions_page_when_authenticated", async () => {
    mockedGetToken.mockReturnValue("valid-token");

    renderWithRouter("/transactions");

    await screen.findByRole("heading", { name: "Transactions" });
  });

  it("redirects_root_to_transactions", async () => {
    mockedGetToken.mockReturnValue("valid-token");

    const router = renderWithRouter("/");

    await screen.findByRole("heading", { name: "Transactions" });
    expect(router.state.location.pathname).toBe("/transactions");
  });

  it("returns_login_page_without_authentication", async () => {
    renderWithRouter("/login");

    await screen.findByRole("heading", { name: "Login" });
  });
});
