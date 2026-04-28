import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import type { Transaction } from "../types";
import { TransactionList } from "./TransactionList";

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

describe("TransactionList", () => {
  it("renders the default empty state when there are no transactions", () => {
    render(<TransactionList transactions={[]} />);

    expect(
      screen.getByText("No transactions yet. Tap + to add your first one!"),
    ).toBeInTheDocument();
  });

  it("renders the provided emptyMessage when there are no transactions", () => {
    render(
      <TransactionList transactions={[]} emptyMessage="No transactions match your filters." />,
    );

    expect(screen.getByText("No transactions match your filters.")).toBeInTheDocument();
  });

  it("renders one date group per distinct date", () => {
    const transactions = [
      createTransaction({ id: 1, date: "2026-02-23", title: "Lunch" }),
      createTransaction({ id: 2, date: "2026-02-23", title: "Train fare" }),
      createTransaction({ id: 3, date: "2026-02-22", title: "Game" }),
    ];

    render(<TransactionList transactions={transactions} />);

    const groups = screen.getAllByRole("group");
    expect(groups).toHaveLength(2);
  });

  it("formats the date header as 'Feb 23, Sun'", () => {
    render(
      <TransactionList transactions={[createTransaction({ date: "2026-02-22", title: "Game" })]} />,
    );

    // 2026-02-22 is a Sunday
    expect(screen.getByRole("heading", { name: "Feb 22, Sun" })).toBeInTheDocument();
  });

  it("preserves the order received from the API within each date group", () => {
    const transactions = [
      createTransaction({ id: 10, date: "2026-02-23", title: "Lunch" }),
      createTransaction({ id: 5, date: "2026-02-23", title: "Train fare" }),
    ];

    render(<TransactionList transactions={transactions} />);

    const group = screen.getByRole("group", { name: /feb 23/i });
    const titles = within(group).getAllByText(/lunch|train fare/i);
    expect(titles[0]).toHaveTextContent("Lunch");
    expect(titles[1]).toHaveTextContent("Train fare");
  });

  it("keeps dates separated in the order the API returned them", () => {
    const transactions = [
      createTransaction({ id: 1, date: "2026-02-23", title: "Lunch" }),
      createTransaction({ id: 2, date: "2026-02-22", title: "Game" }),
    ];

    render(<TransactionList transactions={transactions} />);

    const headings = screen.getAllByRole("heading");
    expect(headings[0]).toHaveTextContent("Feb 23");
    expect(headings[1]).toHaveTextContent("Feb 22");
  });

  it("calls onSelect with the transaction when a row is clicked", async () => {
    const user = userEvent.setup();
    const onSelect = vi.fn();
    const transaction = createTransaction({ id: 1, title: "Lunch" });

    render(<TransactionList transactions={[transaction]} onSelect={onSelect} />);

    await user.click(screen.getByRole("button", { name: /lunch/i }));

    expect(onSelect).toHaveBeenCalledWith(transaction);
  });
});
