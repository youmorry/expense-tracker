import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import type { CategoryAnalytics } from "../types";
import { CategoryBreakdown } from "./CategoryBreakdown";

function createAnalytics(overrides: Partial<CategoryAnalytics> = {}): CategoryAnalytics {
  return {
    totalAmount: "12000",
    categories: [
      {
        categoryId: 1,
        categoryName: "Food",
        amount: "8000",
        percentage: 66.7,
        transactionCount: 4,
      },
      {
        categoryId: 2,
        categoryName: "Transport",
        amount: "4000",
        percentage: 33.3,
        transactionCount: 2,
      },
    ],
    ...overrides,
  };
}

describe("CategoryBreakdown", () => {
  it("renders the empty state when no categories are present", () => {
    render(<CategoryBreakdown data={createAnalytics({ totalAmount: "0", categories: [] })} />);

    expect(screen.getByText("No data for this period.")).toBeInTheDocument();
  });

  it("renders each category with its emoji, name, formatted amount and percentage", () => {
    render(<CategoryBreakdown data={createAnalytics()} />);

    expect(screen.getByText("Food")).toBeInTheDocument();
    expect(screen.getByText("🍽")).toBeInTheDocument();
    expect(screen.getByText(/8,000/)).toBeInTheDocument();
    expect(screen.getByText(/66\.7%/)).toBeInTheDocument();

    expect(screen.getByText("Transport")).toBeInTheDocument();
    expect(screen.getByText("🚃")).toBeInTheDocument();
    expect(screen.getByText(/4,000/)).toBeInTheDocument();
    expect(screen.getByText(/33\.3%/)).toBeInTheDocument();
  });

  it("preserves the API-provided category order in the list", () => {
    render(<CategoryBreakdown data={createAnalytics()} />);

    const items = screen.getAllByRole("listitem");
    expect(items).toHaveLength(2);
    expect(items[0]).toHaveTextContent("Food");
    expect(items[1]).toHaveTextContent("Transport");
  });

  it("falls back to the Uncategorized emoji when the category name is unknown", () => {
    render(
      <CategoryBreakdown
        data={createAnalytics({
          categories: [
            {
              categoryId: 99,
              categoryName: "Mystery",
              amount: "1000",
              percentage: 100,
              transactionCount: 1,
            },
          ],
        })}
      />,
    );

    expect(screen.getByText("➖")).toBeInTheDocument();
  });
});
