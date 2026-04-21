import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { EmptyState } from "@/components/EmptyState";

describe("EmptyState", () => {
  it("renders the title", () => {
    render(<EmptyState title="No transactions yet" />);

    expect(screen.getByText("No transactions yet")).toBeInTheDocument();
  });

  it("renders the description when provided", () => {
    render(<EmptyState title="No transactions yet" description="Tap + to add your first one!" />);

    expect(screen.getByText("Tap + to add your first one!")).toBeInTheDocument();
  });

  it("renders the icon slot when provided", () => {
    render(
      <EmptyState
        title="No transactions yet"
        icon={<svg data-testid="custom-icon" aria-hidden="true" />}
      />,
    );

    expect(screen.getByTestId("custom-icon")).toBeInTheDocument();
  });

  it("renders the action slot when provided", () => {
    render(
      <EmptyState
        title="No transactions match your filters"
        action={<button type="button">Clear filters</button>}
      />,
    );

    expect(screen.getByRole("button", { name: /clear filters/i })).toBeInTheDocument();
  });
});
