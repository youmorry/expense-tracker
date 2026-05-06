import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { ToastProvider } from "../../../components/Toast";
import type { Transaction } from "../../../types/api";
import { server } from "../../../test/mocks/server";
import { TransactionFormModal } from "./TransactionFormModal";

const CURRENCY_KEY = "expense-tracker:currency";

const EDIT_TRANSACTION: Transaction = {
  id: 99,
  date: "2026-04-20",
  amount: "500",
  categoryId: 1,
  categoryName: "Food",
  needWantType: "NEED",
  title: "Old Lunch",
  memo: "with friends",
  createdAt: "2026-04-20T10:00:00Z",
  updatedAt: "2026-04-20T10:00:00Z",
};

function renderModal(
  props: { open?: boolean; onClose?: () => void; transaction?: Transaction } = {},
) {
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
          <TransactionFormModal
            open={props.open ?? true}
            onClose={onClose}
            {...(props.transaction !== undefined ? { transaction: props.transaction } : {})}
          />
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

  it("shows an error toast when the API returns 422 without a field errors array", async () => {
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

  describe("422 with field errors", () => {
    function setUpValidationErrorHandler(errors: { pointer: string; detail: string }[]) {
      server.use(
        http.post("/api/v1/transactions", () => {
          return HttpResponse.json(
            {
              type: "/errors/validation-error",
              title: "Your request is not valid.",
              status: 422,
              detail: "One or more fields have validation errors.",
              errors,
            },
            { status: 422 },
          );
        }),
      );
    }

    it("renders inline errors under the corresponding fields", async () => {
      setUpValidationErrorHandler([
        { pointer: "#/amount", detail: "must be greater than 0" },
        { pointer: "#/category_id", detail: "category not found" },
        { pointer: "#/need_want_type", detail: "need_want_type is required" },
      ]);
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      renderModal();

      await user.type(screen.getByLabelText(/amount/i), "10");
      await user.click(screen.getByRole("button", { name: /save/i }));

      const needWantError = await screen.findByText(/need_want_type is required/i);
      expect(screen.getByText(/must be greater than 0/i)).toBeInTheDocument();
      expect(screen.getByText(/category not found/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/amount/i)).toHaveAttribute("aria-invalid", "true");
      const toggleGroup = screen.getByRole("group");
      expect(toggleGroup).toHaveAttribute("aria-invalid", "true");
      expect(toggleGroup.getAttribute("aria-describedby")).toBe(needWantError.id);
    });

    it("does not show a toast when inline field errors are present", async () => {
      setUpValidationErrorHandler([{ pointer: "#/amount", detail: "must be greater than 0" }]);
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      renderModal();

      await user.type(screen.getByLabelText(/amount/i), "10");
      await user.click(screen.getByRole("button", { name: /save/i }));

      const inlineMessages = await screen.findAllByText(/must be greater than 0/i);
      expect(inlineMessages).toHaveLength(1);
    });

    it("clears the inline error for a field when its input changes", async () => {
      setUpValidationErrorHandler([
        { pointer: "#/title", detail: "must be at most 200 characters" },
      ]);
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      renderModal();

      await user.type(screen.getByLabelText(/amount/i), "10");
      await user.click(screen.getByRole("button", { name: /save/i }));

      expect(await screen.findByText(/must be at most 200 characters/i)).toBeInTheDocument();

      await user.type(screen.getByLabelText(/title/i), "shorter");

      expect(screen.queryByText(/must be at most 200 characters/i)).not.toBeInTheDocument();
    });
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

  it("does not show a Delete button in create mode", () => {
    renderModal();

    expect(screen.queryByRole("button", { name: /delete/i })).not.toBeInTheDocument();
  });

  describe("edit mode", () => {
    it("renders 'Edit Transaction' title when transaction prop is provided", () => {
      renderModal({ transaction: EDIT_TRANSACTION });

      expect(screen.getByRole("dialog", { name: /edit transaction/i })).toBeInTheDocument();
    });

    it("pre-populates form fields with the transaction data", () => {
      renderModal({ transaction: EDIT_TRANSACTION });

      expect(screen.getByLabelText(/date/i)).toHaveValue("2026-04-20");
      expect(screen.getByLabelText(/amount/i)).toHaveValue("500");
      expect(screen.getByLabelText(/title/i)).toHaveValue("Old Lunch");
      expect(screen.getByLabelText(/memo/i)).toHaveValue("with friends");
    });

    it("calls PUT endpoint on save in edit mode", async () => {
      let capturedBody: unknown;
      server.use(
        http.put("/api/v1/transactions/:id", async ({ request }) => {
          capturedBody = await request.json();
          return HttpResponse.json({
            id: 99,
            date: "2026-04-20",
            amount: "750",
            category_id: 1,
            category_name: "Food",
            need_want_type: "NEED",
            title: "Old Lunch",
            memo: "with friends",
            created_at: "2026-04-20T10:00:00Z",
            updated_at: "2026-04-20T10:00:00Z",
          });
        }),
      );
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      renderModal({ transaction: EDIT_TRANSACTION });

      await user.clear(screen.getByLabelText(/amount/i));
      await user.type(screen.getByLabelText(/amount/i), "750");
      await user.click(screen.getByRole("button", { name: /save/i }));

      await waitFor(() => {
        expect(capturedBody).toMatchObject({ amount: "750" });
      });
    });

    it("shows 'Transaction updated' toast on success", async () => {
      server.use(
        http.put("/api/v1/transactions/:id", () => {
          return HttpResponse.json({
            id: 99,
            date: "2026-04-20",
            amount: "500",
            category_id: 1,
            category_name: "Food",
            need_want_type: "NEED",
            title: "Old Lunch",
            memo: "with friends",
            created_at: "2026-04-20T10:00:00Z",
            updated_at: "2026-04-20T10:00:00Z",
          });
        }),
      );
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      renderModal({ transaction: EDIT_TRANSACTION });

      await user.click(screen.getByRole("button", { name: /save/i }));

      expect(await screen.findByText(/transaction updated/i)).toBeInTheDocument();
    });

    it("shows a Delete button in edit mode", () => {
      renderModal({ transaction: EDIT_TRANSACTION });

      expect(screen.getByRole("button", { name: /delete/i })).toBeInTheDocument();
    });

    it("opens the delete confirm dialog when the Delete button is clicked", async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      renderModal({ transaction: EDIT_TRANSACTION });

      await user.click(screen.getByRole("button", { name: /^delete$/i }));

      expect(await screen.findByRole("alertdialog")).toBeInTheDocument();
      expect(screen.getByText(/delete this transaction\?/i)).toBeInTheDocument();
    });

    it("calls DELETE endpoint and closes the modal when delete is confirmed", async () => {
      let capturedUrl = "";
      let capturedMethod = "";
      server.use(
        http.delete("/api/v1/transactions/:id", ({ request }) => {
          capturedUrl = request.url;
          capturedMethod = request.method;
          return new HttpResponse(null, { status: 204 });
        }),
      );
      const onClose = vi.fn();
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      renderModal({ transaction: EDIT_TRANSACTION, onClose });

      await user.click(screen.getByRole("button", { name: /^delete$/i }));
      const dialog = await screen.findByRole("alertdialog");
      await user.click(within(dialog).getByRole("button", { name: /^delete$/i }));

      await waitFor(() => {
        expect(onClose).toHaveBeenCalled();
      });
      expect(capturedMethod).toBe("DELETE");
      expect(capturedUrl).toContain("/api/v1/transactions/99");
      expect(await screen.findByText(/transaction deleted/i)).toBeInTheDocument();
    });

    it("closes the confirm dialog only when delete is canceled", async () => {
      const onClose = vi.fn();
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      renderModal({ transaction: EDIT_TRANSACTION, onClose });

      await user.click(screen.getByRole("button", { name: /^delete$/i }));
      const dialog = await screen.findByRole("alertdialog");
      await user.click(within(dialog).getByRole("button", { name: /^cancel$/i }));

      await waitFor(() => {
        expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument();
      });
      expect(onClose).not.toHaveBeenCalled();
      expect(screen.getByRole("dialog", { name: /edit transaction/i })).toBeInTheDocument();
    });

    it("shows an error toast when the DELETE API responds with an error", async () => {
      server.use(
        http.delete("/api/v1/transactions/:id", () => {
          return HttpResponse.json(
            {
              type: "about:blank",
              title: "Not Found",
              status: 404,
              detail: "transaction not found",
            },
            { status: 404 },
          );
        }),
      );
      const onClose = vi.fn();
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      renderModal({ transaction: EDIT_TRANSACTION, onClose });

      await user.click(screen.getByRole("button", { name: /^delete$/i }));
      const dialog = await screen.findByRole("alertdialog");
      await user.click(within(dialog).getByRole("button", { name: /^delete$/i }));

      expect(await screen.findByText(/transaction not found/i)).toBeInTheDocument();
      expect(onClose).not.toHaveBeenCalled();
    });
  });
});
