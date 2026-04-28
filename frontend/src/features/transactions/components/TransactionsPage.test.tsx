import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { ToastProvider } from "../../../components/Toast";
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
      <ToastProvider>
        <RouterProvider router={router} />
      </ToastProvider>
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

  it("opens the transaction form modal when the FAB is clicked", async () => {
    vi.useRealTimers();
    server.use(http.get("/api/v1/transactions", () => HttpResponse.json({ items: [] })));

    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole("button", { name: /add transaction/i }));

    expect(await screen.findByRole("dialog", { name: /add transaction/i })).toBeInTheDocument();
  });

  it("includes the keyword in the API request when entered in the filter", async () => {
    vi.useRealTimers();
    const requestedUrls: string[] = [];
    server.use(
      http.get("/api/v1/transactions", ({ request }) => {
        requestedUrls.push(request.url);
        return HttpResponse.json({ items: [] });
      }),
    );

    const user = userEvent.setup();
    renderPage();

    await user.click(screen.getByRole("button", { name: /filters/i }));
    await user.type(screen.getByLabelText(/keyword/i), "lunch");

    await vi.waitFor(() => {
      expect(requestedUrls.some((url) => url.includes("keyword=lunch"))).toBe(true);
    });
  });

  it("shows the filter-specific empty message when filters are applied with no results", async () => {
    server.use(http.get("/api/v1/transactions", () => HttpResponse.json({ items: [] })));

    const user = userEvent.setup();
    renderPage();

    await user.click(screen.getByRole("button", { name: /filters/i }));
    await user.type(screen.getByLabelText(/keyword/i), "nothing");

    expect(await screen.findByText("No transactions match your filters.")).toBeInTheDocument();
    expect(
      screen.queryByText("No transactions yet. Tap + to add your first one!"),
    ).not.toBeInTheDocument();
  });

  it("opens the edit modal when a transaction row is clicked", async () => {
    vi.useRealTimers();
    server.use(
      http.get("/api/v1/transactions", () => {
        return HttpResponse.json({
          items: [
            {
              id: 5,
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

    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole("button", { name: /lunch/i }));

    expect(await screen.findByRole("dialog", { name: /edit transaction/i })).toBeInTheDocument();
  });
});
