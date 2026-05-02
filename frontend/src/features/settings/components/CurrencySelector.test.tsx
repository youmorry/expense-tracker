import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it } from "vitest";

import { CurrencySelector } from "./CurrencySelector";

const STORAGE_KEY = "expense-tracker:currency";

describe("CurrencySelector", () => {
  beforeEach(() => {
    localStorage.setItem(STORAGE_KEY, "JPY");
  });

  afterEach(() => {
    localStorage.clear();
  });

  it("renders current currency with symbol", () => {
    render(<CurrencySelector />);

    expect(screen.getByRole("button", { name: /currency/i })).toHaveTextContent("JPY (¥)");
  });

  it("opens currency selection dialog when trigger is clicked", async () => {
    const user = userEvent.setup();
    render(<CurrencySelector />);

    await user.click(screen.getByRole("button", { name: /currency/i }));

    expect(screen.getByRole("dialog", { name: /select currency/i })).toBeInTheDocument();
    expect(screen.getByRole("radio", { name: /JPY/ })).toBeChecked();
    expect(screen.getByRole("radio", { name: /USD/ })).toBeInTheDocument();
  });

  it("shows confirmation dialog when a different currency is selected", async () => {
    const user = userEvent.setup();
    render(<CurrencySelector />);

    await user.click(screen.getByRole("button", { name: /currency/i }));
    await user.click(screen.getByRole("radio", { name: /USD/ }));

    expect(
      screen.getByText(/changing currency only affects how amounts are displayed/i),
    ).toBeInTheDocument();
  });

  it("persists new currency to localStorage when confirmed", async () => {
    const user = userEvent.setup();
    render(<CurrencySelector />);

    await user.click(screen.getByRole("button", { name: /currency/i }));
    await user.click(screen.getByRole("radio", { name: /USD/ }));
    await user.click(screen.getByRole("button", { name: /change/i }));

    expect(localStorage.getItem(STORAGE_KEY)).toBe("USD");
    expect(screen.getByRole("button", { name: /currency/i })).toHaveTextContent("USD ($)");
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("does not persist new currency when cancelled", async () => {
    const user = userEvent.setup();
    render(<CurrencySelector />);

    await user.click(screen.getByRole("button", { name: /currency/i }));
    await user.click(screen.getByRole("radio", { name: /USD/ }));
    await user.click(screen.getByRole("button", { name: /keep current/i }));

    expect(localStorage.getItem(STORAGE_KEY)).toBe("JPY");
    expect(screen.getByRole("dialog", { name: /select currency/i })).toBeInTheDocument();
  });

  it("closes selection dialog without confirmation when current currency is selected", async () => {
    const user = userEvent.setup();
    render(<CurrencySelector />);

    await user.click(screen.getByRole("button", { name: /currency/i }));
    await user.click(screen.getByRole("radio", { name: /JPY/ }));

    expect(localStorage.getItem(STORAGE_KEY)).toBe("JPY");
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });
});
