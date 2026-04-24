import { http, HttpResponse } from "msw";

import type { HttpHandler } from "msw";

export const handlers: HttpHandler[] = [
  http.post("/api/v1/auth/google", () => {
    return HttpResponse.json({
      access_token: "mock-jwt-token",
      user: {
        id: 1,
        email: "test@example.com",
        display_name: "Test User",
        created_at: "2026-01-01T00:00:00Z",
      },
    });
  }),
  http.get("/api/v1/transactions", () => {
    return HttpResponse.json({ items: [] });
  }),
];
