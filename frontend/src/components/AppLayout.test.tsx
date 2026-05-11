import { act, render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { afterEach, describe, expect, it, vi } from "vitest";

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
  afterEach(() => {
    vi.restoreAllMocks();
  });

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

  it("scrolls main to top on route change", async () => {
    const scrollToSpy = vi.spyOn(Element.prototype, "scrollTo");
    const router = renderWithRouter("/transactions");
    await screen.findByRole("heading", { name: "Transactions" });
    scrollToSpy.mockClear();

    await act(async () => {
      await router.navigate("/analytics");
    });

    expect(scrollToSpy).toHaveBeenCalledWith({ top: 0, behavior: "instant" });
    const mainElement = screen.getByRole("main");
    expect(scrollToSpy.mock.instances).toContain(mainElement);
  });
});
