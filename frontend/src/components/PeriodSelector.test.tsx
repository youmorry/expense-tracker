import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { PeriodSelector, type Period } from "./PeriodSelector";

describe("PeriodSelector", () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    vi.setSystemTime(new Date("2026-02-15T12:00:00Z"));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("renders the current month label by default", () => {
    render(<PeriodSelector onChange={vi.fn()} />);

    expect(screen.getByText("February 2026")).toBeInTheDocument();
  });

  it("notifies the current month range on mount", () => {
    const onChange = vi.fn<(period: Period) => void>();

    render(<PeriodSelector onChange={onChange} />);

    expect(onChange).toHaveBeenCalledWith({ from: "2026-02-01", to: "2026-02-28" });
  });

  it("navigates to the previous month when the previous button is clicked", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    const onChange = vi.fn<(period: Period) => void>();
    render(<PeriodSelector onChange={onChange} />);

    await user.click(screen.getByRole("button", { name: /previous period/i }));

    expect(screen.getByText("January 2026")).toBeInTheDocument();
    expect(onChange).toHaveBeenLastCalledWith({ from: "2026-01-01", to: "2026-01-31" });
  });

  it("navigates to the next month when the next button is clicked", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    const onChange = vi.fn<(period: Period) => void>();
    render(<PeriodSelector onChange={onChange} />);

    await user.click(screen.getByRole("button", { name: /next period/i }));

    expect(screen.getByText("March 2026")).toBeInTheDocument();
    expect(onChange).toHaveBeenLastCalledWith({ from: "2026-03-01", to: "2026-03-31" });
  });

  it("wraps to December of the previous year when going back from January", async () => {
    vi.setSystemTime(new Date("2026-01-10T12:00:00Z"));
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    const onChange = vi.fn<(period: Period) => void>();
    render(<PeriodSelector onChange={onChange} />);

    await user.click(screen.getByRole("button", { name: /previous period/i }));

    expect(screen.getByText("December 2025")).toBeInTheDocument();
    expect(onChange).toHaveBeenLastCalledWith({ from: "2025-12-01", to: "2025-12-31" });
  });

  it("computes the last day of February in a leap year", () => {
    vi.setSystemTime(new Date("2024-02-15T12:00:00Z"));
    const onChange = vi.fn<(period: Period) => void>();

    render(<PeriodSelector onChange={onChange} />);

    expect(onChange).toHaveBeenCalledWith({ from: "2024-02-01", to: "2024-02-29" });
  });

  it("switches to the year view with the current year when Year is selected", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    const onChange = vi.fn<(period: Period) => void>();
    render(<PeriodSelector onChange={onChange} />);

    await user.click(screen.getByRole("button", { name: /^year$/i }));

    expect(screen.getByText("2026")).toBeInTheDocument();
    expect(onChange).toHaveBeenLastCalledWith({ from: "2026-01-01", to: "2026-12-31" });
  });

  it("navigates to the previous year when in year view", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    const onChange = vi.fn<(period: Period) => void>();
    render(<PeriodSelector onChange={onChange} />);

    await user.click(screen.getByRole("button", { name: /^year$/i }));
    await user.click(screen.getByRole("button", { name: /previous period/i }));

    expect(screen.getByText("2025")).toBeInTheDocument();
    expect(onChange).toHaveBeenLastCalledWith({ from: "2025-01-01", to: "2025-12-31" });
  });

  it("navigates to the next year when in year view", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    const onChange = vi.fn<(period: Period) => void>();
    render(<PeriodSelector onChange={onChange} />);

    await user.click(screen.getByRole("button", { name: /^year$/i }));
    await user.click(screen.getByRole("button", { name: /next period/i }));

    expect(screen.getByText("2027")).toBeInTheDocument();
    expect(onChange).toHaveBeenLastCalledWith({ from: "2027-01-01", to: "2027-12-31" });
  });

  it("shows 'All Transactions' and notifies null when All is selected", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    const onChange = vi.fn<(period: Period) => void>();
    render(<PeriodSelector onChange={onChange} />);

    await user.click(screen.getByRole("button", { name: /^all$/i }));

    expect(screen.getByText("All Transactions")).toBeInTheDocument();
    expect(onChange).toHaveBeenLastCalledWith(null);
  });

  it("hides previous and next buttons when All is selected", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    render(<PeriodSelector onChange={vi.fn()} />);

    await user.click(screen.getByRole("button", { name: /^all$/i }));

    expect(screen.queryByRole("button", { name: /previous period/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /next period/i })).not.toBeInTheDocument();
  });

  it("restores the month view with the current month when switching back from All", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    const onChange = vi.fn<(period: Period) => void>();
    render(<PeriodSelector onChange={onChange} />);

    await user.click(screen.getByRole("button", { name: /^all$/i }));
    await user.click(screen.getByRole("button", { name: /^month$/i }));

    expect(screen.getByText("February 2026")).toBeInTheDocument();
    expect(onChange).toHaveBeenLastCalledWith({ from: "2026-02-01", to: "2026-02-28" });
  });
});
