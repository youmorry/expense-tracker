import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { createMemoryRouter, RouterProvider } from "react-router";
import { describe, expect, it, vi } from "vitest";

import { server } from "../../../test/mocks/server";
import SettingsPage from "./SettingsPage";

vi.mock("../../../lib/auth", () => ({
  getToken: vi.fn(() => "existing-token"),
  setToken: vi.fn(),
  clearToken: vi.fn(),
}));

const { clearToken } = await import("../../../lib/auth");
const mockClearToken = vi.mocked(clearToken);

function renderSettingsPage(queryClient?: QueryClient) {
  const client =
    queryClient ??
    new QueryClient({
      defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
    });
  const router = createMemoryRouter(
    [
      { path: "/settings", Component: SettingsPage },
      { path: "/login", element: <div>Login page</div> },
    ],
    { initialEntries: ["/settings"] },
  );
  render(
    <QueryClientProvider client={client}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  );
  return { queryClient: client, router };
}

describe("SettingsPage", () => {
  it("renders user email and display name when user is loaded", async () => {
    renderSettingsPage();

    expect(await screen.findByText("test@example.com")).toBeInTheDocument();
    expect(screen.getByText("Test User")).toBeInTheDocument();
  });

  it("clears token and redirects to login when log out is clicked", async () => {
    const user = userEvent.setup();
    mockClearToken.mockClear();

    const { router } = renderSettingsPage();
    await screen.findByText("test@example.com");

    await user.click(screen.getByRole("button", { name: /log out/i }));

    await waitFor(() => {
      expect(mockClearToken).toHaveBeenCalledTimes(1);
    });
    expect(router.state.location.pathname).toBe("/login");
  });

  it("clears query cache when log out is clicked", async () => {
    const user = userEvent.setup();
    mockClearToken.mockClear();

    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
    });
    queryClient.setQueryData(["transactions", { from: "2026-01-01" }], { items: ["stale"] });
    queryClient.setQueryData(["analytics"], { stale: true });

    renderSettingsPage(queryClient);
    await screen.findByText("test@example.com");

    await user.click(screen.getByRole("button", { name: /log out/i }));

    await waitFor(() => {
      expect(queryClient.getQueryData(["transactions", { from: "2026-01-01" }])).toBeUndefined();
      expect(queryClient.getQueryData(["analytics"])).toBeUndefined();
      expect(queryClient.getQueryData(["users", "me"])).toBeUndefined();
    });
  });

  it("redirects to login when user query fails", async () => {
    server.use(
      http.get("/api/v1/users/me", () =>
        HttpResponse.json(
          { type: "about:blank", title: "Internal Server Error", status: 500 },
          { status: 500 },
        ),
      ),
    );
    mockClearToken.mockClear();

    const { router } = renderSettingsPage();

    await waitFor(() => {
      expect(router.state.location.pathname).toBe("/login");
    });
    expect(mockClearToken).toHaveBeenCalled();
  });

  it("renders Delete Account button in the Danger Zone", async () => {
    renderSettingsPage();
    await screen.findByText("test@example.com");

    expect(screen.getByRole("button", { name: /delete account/i })).toBeInTheDocument();
  });

  it("opens confirm dialog when Delete Account button is clicked", async () => {
    const user = userEvent.setup();
    renderSettingsPage();
    await screen.findByText("test@example.com");

    await user.click(screen.getByRole("button", { name: /delete account/i }));

    expect(
      screen.getByText(
        "This will permanently delete your account and all transactions. This action cannot be undone.",
      ),
    ).toBeInTheDocument();
  });

  it("closes the confirm dialog when cancel is clicked", async () => {
    const user = userEvent.setup();
    renderSettingsPage();
    await screen.findByText("test@example.com");

    await user.click(screen.getByRole("button", { name: /delete account/i }));
    await user.click(screen.getByRole("button", { name: /cancel/i }));

    await waitFor(() => {
      expect(
        screen.queryByText(
          "This will permanently delete your account and all transactions. This action cannot be undone.",
        ),
      ).not.toBeInTheDocument();
    });
  });

  it("clears token and redirects to login when delete is confirmed", async () => {
    const user = userEvent.setup();
    mockClearToken.mockClear();

    const { router } = renderSettingsPage();
    await screen.findByText("test@example.com");

    await user.click(screen.getByRole("button", { name: /delete account/i }));
    await user.click(screen.getByRole("button", { name: /^delete$/i }));

    await waitFor(() => {
      expect(mockClearToken).toHaveBeenCalledTimes(1);
    });
    expect(router.state.location.pathname).toBe("/login");
  });

  it("clears query cache when delete is confirmed", async () => {
    const user = userEvent.setup();
    mockClearToken.mockClear();

    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
    });
    queryClient.setQueryData(["transactions", { from: "2026-01-01" }], { items: ["stale"] });
    queryClient.setQueryData(["analytics"], { stale: true });

    renderSettingsPage(queryClient);
    await screen.findByText("test@example.com");

    await user.click(screen.getByRole("button", { name: /delete account/i }));
    await user.click(screen.getByRole("button", { name: /^delete$/i }));

    await waitFor(() => {
      expect(queryClient.getQueryData(["transactions", { from: "2026-01-01" }])).toBeUndefined();
      expect(queryClient.getQueryData(["analytics"])).toBeUndefined();
      expect(queryClient.getQueryData(["users", "me"])).toBeUndefined();
    });
  });

  it("issues DELETE /api/v1/users/me when delete is confirmed", async () => {
    const user = userEvent.setup();
    let capturedMethod = "";
    let capturedUrl = "";
    server.use(
      http.delete("/api/v1/users/me", ({ request }) => {
        capturedMethod = request.method;
        capturedUrl = request.url;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    renderSettingsPage();
    await screen.findByText("test@example.com");

    await user.click(screen.getByRole("button", { name: /delete account/i }));
    await user.click(screen.getByRole("button", { name: /^delete$/i }));

    await waitFor(() => {
      expect(capturedMethod).toBe("DELETE");
    });
    expect(capturedUrl).toContain("/api/v1/users/me");
  });
});
