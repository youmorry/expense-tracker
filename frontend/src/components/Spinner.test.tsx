import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { Spinner } from "@/components/Spinner";

describe("Spinner", () => {
  it("renders a status element with a default accessible label", () => {
    render(<Spinner />);

    expect(screen.getByRole("status", { name: /loading/i })).toBeInTheDocument();
  });

  it("renders a status element with the provided label", () => {
    render(<Spinner label="Fetching transactions" />);

    expect(screen.getByRole("status", { name: /fetching transactions/i })).toBeInTheDocument();
  });
});
