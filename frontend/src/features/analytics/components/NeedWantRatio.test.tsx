import { render, screen, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import type { NeedWantAnalytics } from "../types";
import { NeedWantRatio } from "./NeedWantRatio";

function createAnalytics(overrides: Partial<NeedWantAnalytics> = {}): NeedWantAnalytics {
  return {
    totalAmount: "130000",
    breakdown: [
      { type: "NEED", amount: "80000", percentage: 61.5, transactionCount: 20 },
      { type: "WANT", amount: "35000", percentage: 26.9, transactionCount: 10 },
      { type: "UNSET", amount: "15000", percentage: 11.5, transactionCount: 3 },
    ],
    ...overrides,
  };
}

describe("NeedWantRatio", () => {
  it("renders the empty state when there are no transactions in the breakdown", () => {
    render(
      <NeedWantRatio
        data={createAnalytics({
          totalAmount: "0",
          breakdown: [
            { type: "NEED", amount: "0", percentage: 0, transactionCount: 0 },
            { type: "WANT", amount: "0", percentage: 0, transactionCount: 0 },
            { type: "UNSET", amount: "0", percentage: 0, transactionCount: 0 },
          ],
        })}
      />,
    );

    expect(screen.getByText("No data for this period.")).toBeInTheDocument();
  });

  it("renders the segments when transactions exist even if the total amount is zero", () => {
    render(
      <NeedWantRatio
        data={createAnalytics({
          totalAmount: "0",
          breakdown: [
            { type: "NEED", amount: "1000", percentage: 50, transactionCount: 1 },
            { type: "WANT", amount: "-1000", percentage: 50, transactionCount: 1 },
            { type: "UNSET", amount: "0", percentage: 0, transactionCount: 0 },
          ],
        })}
      />,
    );

    expect(screen.queryByText("No data for this period.")).not.toBeInTheDocument();
    expect(screen.getByRole("listitem", { name: /need/i })).toBeInTheDocument();
    expect(screen.getByRole("listitem", { name: /want/i })).toBeInTheDocument();
  });

  it("renders each segment with its label, formatted amount and percentage", () => {
    render(<NeedWantRatio data={createAnalytics()} />);

    const needRow = screen.getByRole("listitem", { name: /need/i });
    expect(within(needRow).getByText(/80,000/)).toBeInTheDocument();
    expect(within(needRow).getByText(/61\.5%/)).toBeInTheDocument();

    const wantRow = screen.getByRole("listitem", { name: /want/i });
    expect(within(wantRow).getByText(/35,000/)).toBeInTheDocument();
    expect(within(wantRow).getByText(/26\.9%/)).toBeInTheDocument();

    const unsetRow = screen.getByRole("listitem", { name: /unset/i });
    expect(within(unsetRow).getByText(/15,000/)).toBeInTheDocument();
    expect(within(unsetRow).getByText(/11\.5%/)).toBeInTheDocument();
  });

  it("orders the segments NEED, WANT, UNSET regardless of API order", () => {
    render(
      <NeedWantRatio
        data={createAnalytics({
          breakdown: [
            { type: "UNSET", amount: "15000", percentage: 11.5, transactionCount: 3 },
            { type: "WANT", amount: "35000", percentage: 26.9, transactionCount: 10 },
            { type: "NEED", amount: "80000", percentage: 61.5, transactionCount: 20 },
          ],
        })}
      />,
    );

    const items = screen.getAllByRole("listitem");
    expect(items[0]).toHaveTextContent(/need/i);
    expect(items[1]).toHaveTextContent(/want/i);
    expect(items[2]).toHaveTextContent(/unset/i);
  });

  it("shows an unset warning with the transaction count when there are unset transactions", () => {
    render(<NeedWantRatio data={createAnalytics()} />);

    expect(screen.getByText(/3 transactions unset/i)).toBeInTheDocument();
  });

  it("uses singular form when exactly one transaction is unset", () => {
    render(
      <NeedWantRatio
        data={createAnalytics({
          breakdown: [
            { type: "NEED", amount: "80000", percentage: 80, transactionCount: 20 },
            { type: "WANT", amount: "20000", percentage: 20, transactionCount: 10 },
            { type: "UNSET", amount: "0", percentage: 0, transactionCount: 1 },
          ],
        })}
      />,
    );

    expect(screen.getByText(/1 transaction unset/i)).toBeInTheDocument();
  });

  it("hides the unset warning when there are no unset transactions", () => {
    render(
      <NeedWantRatio
        data={createAnalytics({
          breakdown: [
            { type: "NEED", amount: "80000", percentage: 70, transactionCount: 20 },
            { type: "WANT", amount: "35000", percentage: 30, transactionCount: 10 },
            { type: "UNSET", amount: "0", percentage: 0, transactionCount: 0 },
          ],
        })}
      />,
    );

    expect(screen.queryByText(/transactions? unset/i)).not.toBeInTheDocument();
  });
});
