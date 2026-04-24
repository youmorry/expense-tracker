import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { server } from "../../../test/mocks/server";
import TransactionsPage from "./TransactionsPage";

function renderPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  const router = createMemoryRouter(
    [{ path: "/", Component: (): ReactNode => <TransactionsPage /> }],
    { initialEntries: ["/"] },
  );
  return render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  );
}

describe("TransactionsPage", () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    vi.setSystemTime(new Date("2026-02-15T12:00:00Z"));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("requests transactions for the current month on initial render", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ items: [] });
      }),
    );

    renderPage();

    await vi.waitFor(() => {
      expect(capturedUrl).not.toBe("");
    });
    expect(capturedUrl).toContain("from=2026-02-01");
    expect(capturedUrl).toContain("to=2026-02-28");
  });

  it("renders the loaded transactions", async () => {
    server.use(
      http.get("/api/v1/transactions", () => {
        return HttpResponse.json({
          items: [
            {
              id: 1,
              date: "2026-02-23",
              amount: "1200",
              category_id: 1,
              category_name: "Food",
              need_want_type: "NEED",
              title: "Lunch",
              created_at: "2026-02-23T10:30:00Z",
              updated_at: "2026-02-23T10:30:00Z",
            },
          ],
        });
      }),
    );

    renderPage();

    expect(await screen.findByText("Lunch")).toBeInTheDocument();
  });

  it("renders the empty state when the API returns no transactions", async () => {
    server.use(
      http.get("/api/v1/transactions", () => {
        return HttpResponse.json({ items: [] });
      }),
    );

    renderPage();

    expect(
      await screen.findByText("No transactions yet. Tap + to add your first one!"),
    ).toBeInTheDocument();
  });

  it("renders a fixed FAB labelled 'Add transaction'", async () => {
    server.use(http.get("/api/v1/transactions", () => HttpResponse.json({ items: [] })));

    renderPage();

    expect(await screen.findByRole("button", { name: /add transaction/i })).toBeInTheDocument();
  });
});
