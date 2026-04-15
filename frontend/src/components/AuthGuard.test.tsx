import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("../lib/auth", () => ({
  getToken: vi.fn(),
}));

import { getToken } from "../lib/auth";
import { AuthGuard } from "./AuthGuard";

const mockedGetToken = vi.mocked(getToken);

function renderWithAuthGuard(initialEntry: string) {
  const router = createMemoryRouter(
    [
      { path: "/login", element: <h1>Login</h1> },
      {
        Component: AuthGuard,
        children: [{ path: "/protected", element: <h1>Protected</h1> }],
      },
    ],
    { initialEntries: [initialEntry] },
  );
  render(<RouterProvider router={router} />);
  return router;
}

describe("AuthGuard", () => {
  beforeEach(() => {
    mockedGetToken.mockReturnValue(null);
  });

  it("redirects to login when token is absent", async () => {
    const router = renderWithAuthGuard("/protected");

    await screen.findByRole("heading", { name: "Login" });
    expect(router.state.location.pathname).toBe("/login");
  });

  it("renders child route when token is present", async () => {
    mockedGetToken.mockReturnValue("valid-token");

    renderWithAuthGuard("/protected");

    await screen.findByRole("heading", { name: "Protected" });
  });
});
