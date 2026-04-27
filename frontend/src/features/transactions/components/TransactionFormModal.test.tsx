import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import type { ReactNode } from "react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { ToastProvider } from "../../../components/Toast";
import { server } from "../../../test/mocks/server";
import { TransactionFormModal } from "./TransactionFormModal";

const CURRENCY_KEY = "expense-tracker:currency";

function renderModal(props: { open?: boolean; onClose?: () => void } = {}) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
  const onClose = props.onClose ?? vi.fn();
  return {
    onClose,
    ...render(
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          <TransactionFormModal open={props.open ?? true} onClose={onClose} />
        </ToastProvider>
      </QueryClientProvider>,
    ),
  };
}

describe("TransactionFormModal", () => {
  beforeEach(() => {
    localStorage.setItem(CURRENCY_KEY, "USD");
    vi.useFakeTimers({ shouldAdvanceTime: true });
    vi.setSystemTime(new Date("2026-04-27T12:00:00Z"));
  });

  afterEach(() => {
    localStorage.removeItem(CURRENCY_KEY);
    vi.useRealTimers();
  });

  it("renders the form fields when open", () => {
    renderModal();

    expect(screen.getByRole("dialog", { name: /add transaction/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/date/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/amount/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/memo/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /save/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /^cancel$/i })).toBeInTheDocument();
  });

  it("focuses the amount input when opened", async () => {
    renderModal();

    await waitFor(() => {
      expect(screen.getByLabelText(/amount/i)).toHaveFocus();
    });
  });

  it("defaults the date to today", () => {
    renderModal();

    expect(screen.getByLabelText(/date/i)).toHaveValue("2026-04-27");
  });

  it("shows a validation error when amount is empty on submit", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderModal();

    await user.click(screen.getByRole("button", { name: /save/i }));

    expect(await screen.findByText(/amount is required/i)).toBeInTheDocument();
  });

  it("shows a validation error when amount has too many decimal places for the currency", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderModal();

    await user.type(screen.getByLabelText(/amount/i), "1.234");
    await user.click(screen.getByRole("button", { name: /save/i }));

    expect(await screen.findByText(/up to 2 decimal/i)).toBeInTheDocument();
  });

  it("posts the form values and closes the modal on successful save", async () => {
    let capturedBody: unknown;
    server.use(
      http.post("/api/v1/transactions", async ({ request }) => {
        capturedBody = await request.json();
        return HttpResponse.json(
          {
            id: 1,
            date: "2026-04-27",
            amount: "12.50",
            category_id: 11,
            category_name: "Uncategorized",
            need_want_type: "UNSET",
            created_at: "2026-04-27T12:00:00Z",
            updated_at: "2026-04-27T12:00:00Z",
          },
          { status: 201 },
        );
      }),
    );
    const onClose = vi.fn();
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderModal({ onClose });

    await user.type(screen.getByLabelText(/amount/i), "12.50");
    await user.click(screen.getByRole("button", { name: /save/i }));

    await waitFor(() => {
      expect(onClose).toHaveBeenCalled();
    });
    expect(capturedBody).toEqual({
      date: "2026-04-27",
      amount: "12.50",
    });
    expect(await screen.findByText(/transaction saved/i)).toBeInTheDocument();
  });

  it("includes the selected category and need-want type in the request body", async () => {
    let capturedBody: unknown;
    server.use(
      http.post("/api/v1/transactions", async ({ request }) => {
        capturedBody = await request.json();
        return HttpResponse.json(
          {
            id: 1,
            date: "2026-04-27",
            amount: "5.00",
            category_id: 1,
            category_name: "Food",
            need_want_type: "NEED",
            title: "Lunch",
            memo: "with team",
            created_at: "2026-04-27T12:00:00Z",
            updated_at: "2026-04-27T12:00:00Z",
          },
          { status: 201 },
        );
      }),
    );
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderModal();

    await user.type(screen.getByLabelText(/amount/i), "5.00");
    await screen.findByRole("option", { name: /food/i });
    await user.selectOptions(screen.getByLabelText(/category/i), "1");
    await user.click(screen.getByRole("radio", { name: "NEED" }));
    await user.type(screen.getByLabelText(/title/i), "Lunch");
    await user.type(screen.getByLabelText(/memo/i), "with team");
    await user.click(screen.getByRole("button", { name: /save/i }));

    await waitFor(() => {
      expect(capturedBody).toEqual({
        date: "2026-04-27",
        amount: "5.00",
        category_id: 1,
        need_want_type: "NEED",
        title: "Lunch",
        memo: "with team",
      });
    });
  });

  it("shows an error toast when the API responds with an error", async () => {
    server.use(
      http.post("/api/v1/transactions", () => {
        return HttpResponse.json(
          {
            type: "about:blank",
            title: "Validation Error",
            status: 422,
            detail: "amount must be positive",
          },
          { status: 422 },
        );
      }),
    );
    const onClose = vi.fn();
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderModal({ onClose });

    await user.type(screen.getByLabelText(/amount/i), "10");
    await user.click(screen.getByRole("button", { name: /save/i }));

    expect(await screen.findByText(/amount must be positive/i)).toBeInTheDocument();
    expect(onClose).not.toHaveBeenCalled();
  });

  it("closes immediately when cancel is clicked with no input changes", async () => {
    const onClose = vi.fn();
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderModal({ onClose });

    await user.click(screen.getByRole("button", { name: /^cancel$/i }));

    expect(onClose).toHaveBeenCalled();
    expect(screen.queryByText(/discard changes\?/i)).not.toBeInTheDocument();
  });

  it("shows a discard confirm dialog when cancel is clicked with dirty input", async () => {
    const onClose = vi.fn();
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderModal({ onClose });

    await user.type(screen.getByLabelText(/amount/i), "5");
    await user.click(screen.getByRole("button", { name: /^cancel$/i }));

    expect(await screen.findByText(/discard changes\?/i)).toBeInTheDocument();
    expect(onClose).not.toHaveBeenCalled();
  });

  it("closes the modal when discard is confirmed", async () => {
    const onClose = vi.fn();
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderModal({ onClose });

    await user.type(screen.getByLabelText(/amount/i), "5");
    await user.click(screen.getByRole("button", { name: /^cancel$/i }));
    await user.click(await screen.findByRole("button", { name: /discard/i }));

    expect(onClose).toHaveBeenCalled();
  });
});
