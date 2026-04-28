import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import type { Transaction } from "../types";
import { TransactionItem } from "./TransactionItem";

function createTransaction(overrides: Partial<Transaction> = {}): Transaction {
  return {
    id: 1,
    date: "2026-02-23",
    amount: "1200",
    categoryId: 1,
    categoryName: "Food",
    needWantType: "NEED",
    title: "Lunch",
    createdAt: "2026-02-23T10:30:00Z",
    updatedAt: "2026-02-23T10:30:00Z",
    ...overrides,
  };
}

describe("TransactionItem", () => {
  it("renders the category emoji based on the category name", () => {
    render(<TransactionItem transaction={createTransaction({ categoryName: "Food" })} />);

    expect(screen.getByText("🍽")).toBeInTheDocument();
  });

  it("renders the title when provided", () => {
    render(<TransactionItem transaction={createTransaction({ title: "Lunch" })} />);

    expect(screen.getByText("Lunch")).toBeInTheDocument();
  });

  it("falls back to the category name when the title is not provided", () => {
    render(
      <TransactionItem
        transaction={createTransaction({ title: undefined, categoryName: "Coffee" })}
      />,
    );

    expect(screen.getByText("Coffee")).toBeInTheDocument();
  });

  it("renders the formatted amount using the active currency", () => {
    render(<TransactionItem transaction={createTransaction({ amount: "1200" })} />);

    expect(screen.getByText(/1,200/)).toBeInTheDocument();
  });

  it("renders a NEED badge when need_want_type is NEED", () => {
    render(<TransactionItem transaction={createTransaction({ needWantType: "NEED" })} />);

    expect(screen.getByText("NEED")).toBeInTheDocument();
  });

  it("renders a WANT badge when need_want_type is WANT", () => {
    render(<TransactionItem transaction={createTransaction({ needWantType: "WANT" })} />);

    expect(screen.getByText("WANT")).toBeInTheDocument();
  });

  it("hides the need/want badge when need_want_type is UNSET", () => {
    render(<TransactionItem transaction={createTransaction({ needWantType: "UNSET" })} />);

    expect(screen.queryByText("NEED")).not.toBeInTheDocument();
    expect(screen.queryByText("WANT")).not.toBeInTheDocument();
    expect(screen.queryByText("UNSET")).not.toBeInTheDocument();
  });

  it("uses the Uncategorized emoji when the category name is unknown", () => {
    render(<TransactionItem transaction={createTransaction({ categoryName: "Mystery" })} />);

    expect(screen.getByText("➖")).toBeInTheDocument();
  });

  it("calls onClick when the item is clicked", async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();
    render(<TransactionItem transaction={createTransaction()} onClick={onClick} />);

    await user.click(screen.getByRole("button"));

    expect(onClick).toHaveBeenCalled();
  });
});
