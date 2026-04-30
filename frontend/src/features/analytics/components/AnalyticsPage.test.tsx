import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { server } from "../../../test/mocks/server";
import AnalyticsPage from "./AnalyticsPage";

function renderPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  const router = createMemoryRouter(
    [{ path: "/", Component: (): ReactNode => <AnalyticsPage /> }],
    { initialEntries: ["/"] },
  );
  return render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  );
}

describe("AnalyticsPage", () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    vi.setSystemTime(new Date("2026-02-15T12:00:00Z"));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("requests category analytics for the current month on initial render", async () => {
    let capturedUrl = "";
    server.use(
      http.get("/api/v1/analytics/category", ({ request }) => {
        capturedUrl = request.url;
        return HttpResponse.json({ total_amount: "0", categories: [] });
      }),
    );

    renderPage();

    await vi.waitFor(() => {
      expect(capturedUrl).not.toBe("");
    });
    expect(capturedUrl).toContain("from=2026-02-01");
    expect(capturedUrl).toContain("to=2026-02-28");
  });

  it("renders the empty state when the period has no data", async () => {
    server.use(
      http.get("/api/v1/analytics/category", () => {
        return HttpResponse.json({ total_amount: "0", categories: [] });
      }),
    );

    renderPage();

    expect(await screen.findByText("No data for this period.")).toBeInTheDocument();
  });

  it("renders each category from the API response", async () => {
    server.use(
      http.get("/api/v1/analytics/category", () => {
        return HttpResponse.json({
          total_amount: "12000",
          categories: [
            {
              category_id: 1,
              category_name: "Food",
              amount: "8000",
              percentage: 66.7,
              transaction_count: 4,
            },
            {
              category_id: 2,
              category_name: "Transport",
              amount: "4000",
              percentage: 33.3,
              transaction_count: 2,
            },
          ],
        });
      }),
    );

    renderPage();

    expect(await screen.findByText("Food")).toBeInTheDocument();
    expect(screen.getByText("Transport")).toBeInTheDocument();
  });

  it("re-requests analytics with a new period after navigating to the previous month", async () => {
    const requestedUrls: string[] = [];
    server.use(
      http.get("/api/v1/analytics/category", ({ request }) => {
        requestedUrls.push(request.url);
        return HttpResponse.json({ total_amount: "0", categories: [] });
      }),
    );

    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderPage();

    await vi.waitFor(() => {
      expect(requestedUrls.some((url) => url.includes("from=2026-02-01"))).toBe(true);
    });

    await user.click(screen.getByRole("button", { name: /previous period/i }));

    await vi.waitFor(() => {
      expect(requestedUrls.some((url) => url.includes("from=2026-01-01"))).toBe(true);
    });
  });
});
