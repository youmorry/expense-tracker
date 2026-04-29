import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import type { Transaction } from "../../../types/api";
import { DeleteConfirmDialog } from "./DeleteConfirmDialog";

const CURRENCY_KEY = "expense-tracker:currency";

const TRANSACTION: Transaction = {
  id: 99,
  date: "2026-04-20",
  amount: "1500",
  categoryId: 1,
  categoryName: "Food",
  needWantType: "NEED",
  title: "Lunch",
  memo: "with team",
  createdAt: "2026-04-20T10:00:00Z",
  updatedAt: "2026-04-20T10:00:00Z",
};

describe("DeleteConfirmDialog", () => {
  beforeEach(() => {
    localStorage.setItem(CURRENCY_KEY, "USD");
  });

  afterEach(() => {
    localStorage.removeItem(CURRENCY_KEY);
  });

  it("renders the target transaction's date, amount, and category", () => {
    render(
      <DeleteConfirmDialog
        open
        transaction={TRANSACTION}
        isDeleting={false}
        onConfirm={vi.fn()}
        onCancel={vi.fn()}
      />,
    );

    expect(screen.getByText("2026-04-20")).toBeInTheDocument();
    expect(screen.getByText(/\$1,500/)).toBeInTheDocument();
    expect(screen.getByText("Food")).toBeInTheDocument();
  });

  it("renders Delete and Cancel buttons", () => {
    render(
      <DeleteConfirmDialog
        open
        transaction={TRANSACTION}
        isDeleting={false}
        onConfirm={vi.fn()}
        onCancel={vi.fn()}
      />,
    );

    expect(screen.getByRole("button", { name: /^delete$/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /^cancel$/i })).toBeInTheDocument();
  });

  it("calls onConfirm when Delete button is clicked", async () => {
    const onConfirm = vi.fn();
    const user = userEvent.setup();
    render(
      <DeleteConfirmDialog
        open
        transaction={TRANSACTION}
        isDeleting={false}
        onConfirm={onConfirm}
        onCancel={vi.fn()}
      />,
    );

    await user.click(screen.getByRole("button", { name: /^delete$/i }));

    expect(onConfirm).toHaveBeenCalledTimes(1);
  });

  it("calls onCancel when Cancel button is clicked", async () => {
    const onCancel = vi.fn();
    const user = userEvent.setup();
    render(
      <DeleteConfirmDialog
        open
        transaction={TRANSACTION}
        isDeleting={false}
        onConfirm={vi.fn()}
        onCancel={onCancel}
      />,
    );

    await user.click(screen.getByRole("button", { name: /^cancel$/i }));

    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it("disables Delete and Cancel buttons while deleting", () => {
    render(
      <DeleteConfirmDialog
        open
        transaction={TRANSACTION}
        isDeleting
        onConfirm={vi.fn()}
        onCancel={vi.fn()}
      />,
    );

    expect(screen.getByRole("button", { name: /^delete$/i })).toBeDisabled();
    expect(screen.getByRole("button", { name: /^cancel$/i })).toBeDisabled();
  });

  it("does not render when open is false", () => {
    render(
      <DeleteConfirmDialog
        open={false}
        transaction={TRANSACTION}
        isDeleting={false}
        onConfirm={vi.fn()}
        onCancel={vi.fn()}
      />,
    );

    expect(screen.queryByRole("button", { name: /^delete$/i })).not.toBeInTheDocument();
  });
});
