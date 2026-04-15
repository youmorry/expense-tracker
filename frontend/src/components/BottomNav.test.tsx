import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { describe, expect, it } from "vitest";

import { BottomNav } from "./BottomNav";

function renderWithRouter(initialEntry: string) {
  const router = createMemoryRouter(
    [
      {
        element: <BottomNav />,
        children: [
          { path: "/transactions", element: <h1>Transactions</h1> },
          { path: "/analytics", element: <h1>Analytics</h1> },
          { path: "/settings", element: <h1>Settings</h1> },
        ],
      },
    ],
    { initialEntries: [initialEntry] },
  );
  render(<RouterProvider router={router} />);
  return router;
}

describe("BottomNav", () => {
  it("renders three navigation links", () => {
    renderWithRouter("/transactions");

    expect(screen.getByRole("link", { name: /transactions/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /analytics/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /settings/i })).toBeInTheDocument();
  });

  it("links to correct paths", () => {
    renderWithRouter("/transactions");

    expect(screen.getByRole("link", { name: /transactions/i })).toHaveAttribute(
      "href",
      "/transactions",
    );
    expect(screen.getByRole("link", { name: /analytics/i })).toHaveAttribute("href", "/analytics");
    expect(screen.getByRole("link", { name: /settings/i })).toHaveAttribute("href", "/settings");
  });

  it("marks current route as active with aria-current", () => {
    renderWithRouter("/transactions");

    expect(screen.getByRole("link", { name: /transactions/i })).toHaveAttribute(
      "aria-current",
      "page",
    );
    expect(screen.getByRole("link", { name: /analytics/i })).not.toHaveAttribute("aria-current");
    expect(screen.getByRole("link", { name: /settings/i })).not.toHaveAttribute("aria-current");
  });

  it("highlights analytics tab when on analytics route", () => {
    renderWithRouter("/analytics");

    expect(screen.getByRole("link", { name: /analytics/i })).toHaveAttribute(
      "aria-current",
      "page",
    );
    expect(screen.getByRole("link", { name: /transactions/i })).not.toHaveAttribute("aria-current");
  });

  it("highlights settings tab when on settings route", () => {
    renderWithRouter("/settings");

    expect(screen.getByRole("link", { name: /settings/i })).toHaveAttribute("aria-current", "page");
    expect(screen.getByRole("link", { name: /transactions/i })).not.toHaveAttribute("aria-current");
  });
});
