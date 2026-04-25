/**
 * 共通 MSW ハンドラ。Vitest（msw/node）と Playwright/開発（msw/browser）の両方で利用する。
 *
 * `openapi-msw` の `createOpenApiHttp` で path / response を OpenAPI 由来の型に固定し、
 * `backend/openapi.yaml` と乖離した雛形は TS エラーで弾く。
 */

import { createOpenApiHttp } from "openapi-msw";

import type { paths } from "../../types/api-generated";

const http = createOpenApiHttp<paths>();

export const handlers = [
  http.post("/api/v1/auth/google", ({ response }) => {
    return response(200).json({
      access_token: "mock-jwt-token",
      user: {
        id: 1,
        email: "test@example.com",
        display_name: "Test User",
        created_at: "2026-01-01T00:00:00Z",
      },
    });
  }),
  http.get("/api/v1/transactions", ({ response }) => {
    return response(200).json({ items: [] });
  }),
];
