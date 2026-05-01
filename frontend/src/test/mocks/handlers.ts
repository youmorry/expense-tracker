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
  http.post("/api/v1/transactions", async ({ request, response }) => {
    const body = await request.json();
    const now = new Date().toISOString();
    return response(201).json({
      id: Math.floor(Math.random() * 1_000_000) + 1,
      date: body.date,
      amount: body.amount,
      category_id: body.category_id ?? 11,
      category_name: body.category_id !== undefined ? "Food" : "Uncategorized",
      need_want_type: body.need_want_type ?? "UNSET",
      ...(body.title !== undefined ? { title: body.title } : {}),
      ...(body.memo !== undefined ? { memo: body.memo } : {}),
      created_at: now,
      updated_at: now,
    });
  }),
  http.put("/api/v1/transactions/{id}", async ({ request, params, response }) => {
    const body = await request.json();
    const now = new Date().toISOString();
    return response(200).json({
      id: Number(params.id),
      date: body.date,
      amount: body.amount,
      category_id: body.category_id ?? 11,
      category_name: body.category_id !== undefined ? "Food" : "Uncategorized",
      need_want_type: body.need_want_type ?? "UNSET",
      ...(body.title !== undefined ? { title: body.title } : {}),
      ...(body.memo !== undefined ? { memo: body.memo } : {}),
      created_at: now,
      updated_at: now,
    });
  }),
  http.delete("/api/v1/transactions/{id}", ({ response }) => {
    return response(204).empty();
  }),
  http.get("/api/v1/analytics/category", ({ response }) => {
    return response(200).json({ total_amount: "0", categories: [] });
  }),
  http.get("/api/v1/analytics/need-want", ({ response }) => {
    return response(200).json({
      total_amount: "0",
      breakdown: [
        { type: "NEED", amount: "0", percentage: 0, transaction_count: 0 },
        { type: "WANT", amount: "0", percentage: 0, transaction_count: 0 },
        { type: "UNSET", amount: "0", percentage: 0, transaction_count: 0 },
      ],
    });
  }),
  http.get("/api/v1/categories", ({ response }) => {
    return response(200).json({
      items: [
        { id: 1, name: "Food", display_order: 1 },
        { id: 2, name: "Transport", display_order: 2 },
        { id: 3, name: "Housing", display_order: 3 },
        { id: 4, name: "Daily Goods", display_order: 4 },
        { id: 5, name: "Medical", display_order: 5 },
        { id: 6, name: "Entertainment", display_order: 6 },
        { id: 7, name: "Clothing", display_order: 7 },
        { id: 8, name: "Education", display_order: 8 },
        { id: 9, name: "Social", display_order: 9 },
        { id: 10, name: "Other", display_order: 10 },
        { id: 11, name: "Uncategorized", display_order: 11 },
      ],
    });
  }),
];
