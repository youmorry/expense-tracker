import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import {
  defaultPeriodSelectorValue,
  PeriodSelector,
  periodFromValue,
  type PeriodSelectorValue,
} from "./PeriodSelector";

interface HarnessProps {
  initialValue?: PeriodSelectorValue;
  onValueChange?: (value: PeriodSelectorValue) => void;
}

function Harness({ initialValue, onValueChange }: HarnessProps) {
  const [value, setValue] = useState<PeriodSelectorValue>(
    () => initialValue ?? defaultPeriodSelectorValue(),
  );
  return (
    <PeriodSelector
      value={value}
      onChange={(next) => {
        setValue(next);
        onValueChange?.(next);
      }}
    />
  );
}

describe("PeriodSelector", () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    vi.setSystemTime(new Date("2026-02-15T12:00:00Z"));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("renders the current month label by default", () => {
    render(<Harness />);

    expect(screen.getByText("February 2026")).toBeInTheDocument();
  });

  it("notifies a month-unit value and shows the previous month when the previous button is clicked", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    const onValueChange = vi.fn<(value: PeriodSelectorValue) => void>();
    render(<Harness onValueChange={onValueChange} />);

    await user.click(screen.getByRole("button", { name: /previous period/i }));

    expect(screen.getByText("January 2026")).toBeInTheDocument();
    expect(onValueChange).toHaveBeenLastCalledWith(expect.objectContaining({ unit: "month" }));
  });

  it("shows the next month when the next button is clicked", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    render(<Harness />);

    await user.click(screen.getByRole("button", { name: /next period/i }));

    expect(screen.getByText("March 2026")).toBeInTheDocument();
  });

  it("wraps to December of the previous year when going back from January", async () => {
    vi.setSystemTime(new Date("2026-01-10T12:00:00Z"));
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    render(<Harness />);

    await user.click(screen.getByRole("button", { name: /previous period/i }));

    expect(screen.getByText("December 2025")).toBeInTheDocument();
  });

  it("notifies a year-unit value and shows the current year when Year is selected", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    const onValueChange = vi.fn<(value: PeriodSelectorValue) => void>();
    render(<Harness onValueChange={onValueChange} />);

    await user.click(screen.getByRole("radio", { name: /^year$/i }));

    expect(screen.getByText("2026")).toBeInTheDocument();
    expect(onValueChange).toHaveBeenLastCalledWith(expect.objectContaining({ unit: "year" }));
  });

  it("navigates to the previous year when in year view", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    render(<Harness />);

    await user.click(screen.getByRole("radio", { name: /^year$/i }));
    await user.click(screen.getByRole("button", { name: /previous period/i }));

    expect(screen.getByText("2025")).toBeInTheDocument();
  });

  it("navigates to the next year when in year view", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    render(<Harness />);

    await user.click(screen.getByRole("radio", { name: /^year$/i }));
    await user.click(screen.getByRole("button", { name: /next period/i }));

    expect(screen.getByText("2027")).toBeInTheDocument();
  });

  it("notifies an all-unit value and shows 'All Transactions' when All is selected", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    const onValueChange = vi.fn<(value: PeriodSelectorValue) => void>();
    render(<Harness onValueChange={onValueChange} />);

    await user.click(screen.getByRole("radio", { name: /^all$/i }));

    expect(screen.getByText("All Transactions")).toBeInTheDocument();
    expect(onValueChange).toHaveBeenLastCalledWith(expect.objectContaining({ unit: "all" }));
  });

  it("hides previous and next buttons when All is selected", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    render(<Harness />);

    await user.click(screen.getByRole("radio", { name: /^all$/i }));

    expect(screen.queryByRole("button", { name: /previous period/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /next period/i })).not.toBeInTheDocument();
  });

  it("restores the month view with the current month when switching back from All", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    render(<Harness />);

    await user.click(screen.getByRole("radio", { name: /^all$/i }));
    await user.click(screen.getByRole("radio", { name: /^month$/i }));

    expect(screen.getByText("February 2026")).toBeInTheDocument();
  });
});

describe("defaultPeriodSelectorValue", () => {
  it("returns the month unit anchored at the given time", () => {
    const value = defaultPeriodSelectorValue(new Date("2026-02-15T12:00:00Z"));

    expect(value.unit).toBe("month");
    expect(periodFromValue(value)).toEqual({ from: "2026-02-01", to: "2026-02-28" });
  });
});

describe("periodFromValue", () => {
  it("returns the full month range for month unit", () => {
    expect(periodFromValue({ unit: "month", anchor: new Date("2026-02-15T12:00:00Z") })).toEqual({
      from: "2026-02-01",
      to: "2026-02-28",
    });
  });

  it("returns the last day of February in a leap year", () => {
    expect(periodFromValue({ unit: "month", anchor: new Date("2024-02-15T12:00:00Z") })).toEqual({
      from: "2024-02-01",
      to: "2024-02-29",
    });
  });

  it("returns the full year range for year unit", () => {
    expect(periodFromValue({ unit: "year", anchor: new Date("2026-07-01T12:00:00Z") })).toEqual({
      from: "2026-01-01",
      to: "2026-12-31",
    });
  });

  it("returns null for the all unit", () => {
    expect(periodFromValue({ unit: "all", anchor: new Date() })).toBeNull();
  });
});
