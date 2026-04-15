import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { describe, expect, it } from "vitest";

import { AppLayout } from "./AppLayout";

function renderWithRouter(initialEntry: string) {
  const router = createMemoryRouter(
    [
      {
        Component: AppLayout,
        children: [
          { path: "/transactions", element: <h1>Transactions</h1> },
          { path: "/analytics", element: <h1>Analytics</h1> },
        ],
      },
    ],
    { initialEntries: [initialEntry] },
  );
  render(<RouterProvider router={router} />);
  return router;
}

describe("AppLayout", () => {
  it("renders child route content", async () => {
    renderWithRouter("/transactions");

    expect(await screen.findByRole("heading", { name: "Transactions" })).toBeInTheDocument();
  });

  it("renders bottom navigation", () => {
    renderWithRouter("/transactions");

    expect(screen.getByRole("link", { name: /transactions/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /analytics/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /settings/i })).toBeInTheDocument();
  });
});
