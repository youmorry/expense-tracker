import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import type { Category } from "../../../types/api";
import { emptyTransactionFiltersValue, type TransactionFiltersValue } from "../types";
import { TransactionFilters } from "./TransactionFilterPanel";

const CATEGORIES: Category[] = [
  { id: 1, name: "Food", displayOrder: 1 },
  { id: 2, name: "Transport", displayOrder: 2 },
  { id: 3, name: "Housing", displayOrder: 3 },
];

function renderFilters(value: TransactionFiltersValue, onChange = vi.fn()) {
  render(<TransactionFilters value={value} onChange={onChange} categories={CATEGORIES} />);
  return onChange;
}

describe("TransactionFilters", () => {
  it("renders the toggle button collapsed by default with no filter inputs visible", () => {
    renderFilters(emptyTransactionFiltersValue());

    const toggle = screen.getByRole("button", { name: /filters/i });
    expect(toggle).toHaveAttribute("aria-expanded", "false");
    expect(screen.queryByLabelText(/keyword/i)).not.toBeInTheDocument();
  });

  it("expands the panel when the toggle button is clicked", async () => {
    const user = userEvent.setup();
    renderFilters(emptyTransactionFiltersValue());

    await user.click(screen.getByRole("button", { name: /filters/i }));

    expect(screen.getByRole("button", { name: /filters/i })).toHaveAttribute(
      "aria-expanded",
      "true",
    );
    expect(screen.getByLabelText(/keyword/i)).toBeInTheDocument();
  });

  it("shows no active count badge when no filters are applied", () => {
    renderFilters(emptyTransactionFiltersValue());

    expect(screen.queryByLabelText(/active filter count/i)).not.toBeInTheDocument();
  });

  it("shows the active filter count badge reflecting how many filters are set", () => {
    renderFilters({
      keyword: "lunch",
      categoryIds: [1, 2],
      needWantType: "NEED",
    });

    expect(screen.getByLabelText(/active filter count/i)).toHaveTextContent("3");
  });

  it("counts a whitespace-only keyword as inactive", () => {
    renderFilters({
      keyword: "   ",
      categoryIds: [1],
      needWantType: null,
    });

    expect(screen.getByLabelText(/active filter count/i)).toHaveTextContent("1");
  });

  it("emits onChange with the updated keyword when the user types", async () => {
    const user = userEvent.setup();
    const onChange = renderFilters(emptyTransactionFiltersValue());

    await user.click(screen.getByRole("button", { name: /filters/i }));
    await user.type(screen.getByLabelText(/keyword/i), "a");

    expect(onChange).toHaveBeenLastCalledWith({
      keyword: "a",
      categoryIds: [],
      needWantType: null,
    });
  });

  it("emits onChange with the updated need/want when a chip is selected", async () => {
    const user = userEvent.setup();
    const onChange = renderFilters(emptyTransactionFiltersValue());

    await user.click(screen.getByRole("button", { name: /filters/i }));
    await user.click(screen.getByRole("radio", { name: "NEED" }));

    expect(onChange).toHaveBeenLastCalledWith({
      keyword: "",
      categoryIds: [],
      needWantType: "NEED",
    });
  });

  it("emits onChange with null when the same need/want chip is toggled off", async () => {
    const user = userEvent.setup();
    const onChange = renderFilters({ keyword: "", categoryIds: [], needWantType: "WANT" });

    await user.click(screen.getByRole("button", { name: /filters/i }));
    await user.click(screen.getByRole("radio", { name: "WANT" }));

    expect(onChange).toHaveBeenLastCalledWith({
      keyword: "",
      categoryIds: [],
      needWantType: null,
    });
  });

  it("adds a category id to the selection when the checkbox is toggled on", async () => {
    const user = userEvent.setup();
    const onChange = renderFilters(emptyTransactionFiltersValue());

    await user.click(screen.getByRole("button", { name: /filters/i }));
    await user.click(screen.getByRole("button", { name: /categories/i }));
    await user.click(screen.getByRole("checkbox", { name: "Transport" }));

    expect(onChange).toHaveBeenLastCalledWith({
      keyword: "",
      categoryIds: [2],
      needWantType: null,
    });
  });

  it("removes a category id from the selection when its checkbox is toggled off", async () => {
    const user = userEvent.setup();
    const onChange = renderFilters({ keyword: "", categoryIds: [1, 2], needWantType: null });

    await user.click(screen.getByRole("button", { name: /filters/i }));
    await user.click(screen.getByRole("button", { name: /categories/i }));
    await user.click(screen.getByRole("checkbox", { name: "Food" }));

    expect(onChange).toHaveBeenLastCalledWith({
      keyword: "",
      categoryIds: [2],
      needWantType: null,
    });
  });
});
