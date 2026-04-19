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

  it("redirects unauthenticated user from transactions to login", async () => {
    const router = renderWithRouter("/transactions");

    await screen.findByRole("heading", { name: "Login" });
    expect(router.state.location.pathname).toBe("/login");
  });

  it("redirects unauthenticated user from analytics to login", async () => {
    const router = renderWithRouter("/analytics");

    await screen.findByRole("heading", { name: "Login" });
    expect(router.state.location.pathname).toBe("/login");
  });

  it("redirects unauthenticated user from settings to login", async () => {
    const router = renderWithRouter("/settings");

    await screen.findByRole("heading", { name: "Login" });
    expect(router.state.location.pathname).toBe("/login");
  });

  it("returns transactions page when authenticated", async () => {
    mockedGetToken.mockReturnValue("valid-token");

    renderWithRouter("/transactions");

    await screen.findByRole("heading", { name: "Transactions" });
  });

  it("redirects root to transactions", async () => {
    mockedGetToken.mockReturnValue("valid-token");

    const router = renderWithRouter("/");

    await screen.findByRole("heading", { name: "Transactions" });
    expect(router.state.location.pathname).toBe("/transactions");
  });

  it("returns login page without authentication", async () => {
    renderWithRouter("/login");

    await screen.findByRole("heading", { name: "Login" });
  });
});
