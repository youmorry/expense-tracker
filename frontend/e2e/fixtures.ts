/**
 * Playwright の共通ヘルパー。
 *
 * バックエンドは MSW（`src/test/mocks/handlers.ts`）でモックする前提。
 * 個別ケースで応答を変えたいときは `worker.use(...)` を `page.evaluate` 経由で実行する。
 */

import type { QueryClient } from "@tanstack/react-query";
import type { http as mswHttp, HttpResponse as MswHttpResponse } from "msw";
import type { SetupWorker } from "msw/browser";

import type { Page } from "@playwright/test";

import type { components } from "../src/types/api-generated";

type TransactionResponse = components["schemas"]["Transaction"];
type CategoryAnalyticsResponse = components["schemas"]["CategoryAnalytics"];
type NeedWantAnalyticsResponse = components["schemas"]["NeedWantAnalytics"];

declare global {
  interface Window {
    __seedAuthToken?: string;
    __mswWorker?: SetupWorker;
    __mswHttp?: typeof mswHttp;
    __mswHttpResponse?: typeof MswHttpResponse;
    __queryClient?: QueryClient;
  }
}

/**
 * `/api/v1/auth/google` を叩かずに認証済み状態を作る。
 * `main.tsx` が VITE_ENABLE_MSW=true ビルドで `window.__seedAuthToken` を読み、
 * React 起動前にインメモリトークンへ流し込む。
 */
export async function seedAuthToken(page: Page, token = "mock-jwt-token"): Promise<void> {
  await page.addInitScript((value) => {
    window.__seedAuthToken = value;
  }, token);
}

/**
 * MSW のフック（`__mswWorker` / `__mswHttp` / `__mswHttpResponse`）が
 * `window` に公開されるまで待つ。`enableMocking` は非同期なので、
 * `goto` 直後に `worker.use()` を呼ぶ前に必ず通す。
 */
async function waitForMswReady(page: Page): Promise<void> {
  await page.waitForFunction(
    () =>
      typeof window.__mswWorker !== "undefined" &&
      typeof window.__mswHttp !== "undefined" &&
      typeof window.__mswHttpResponse !== "undefined" &&
      typeof window.__queryClient !== "undefined",
  );
}

/**
 * MSW のレスポンス差し替え後に、TanStack Query のキャッシュを破棄して
 * 全クエリを再フェッチさせる。`worker.use()` だけでは既にキャッシュ済みの
 * 結果がそのまま使われるため、表示反映には refetch が必要。
 */
export async function refetchQueries(page: Page): Promise<void> {
  await waitForMswReady(page);
  await page.evaluate(async () => {
    const queryClient = window.__queryClient;
    if (!queryClient) {
      throw new Error(
        "QueryClient is not exposed on window; run with VITE_ENABLE_MSW=true build (npm run dev:e2e / build:e2e)",
      );
    }
    await queryClient.invalidateQueries();
  });
}

/**
 * `GET /api/v1/transactions` のレスポンスを差し替える。デフォルトハンドラは常に
 * 空一覧を返すため、登録 → 一覧反映のような状態遷移シナリオで使う。
 */
export async function mockTransactionList(page: Page, items: TransactionResponse[]): Promise<void> {
  await waitForMswReady(page);
  await page.evaluate((nextItems) => {
    const worker = window.__mswWorker;
    const http = window.__mswHttp;
    const HttpResponse = window.__mswHttpResponse;
    if (!worker || !http || !HttpResponse) {
      throw new Error(
        "MSW E2E hooks are not exposed on window; run with VITE_ENABLE_MSW=true build (npm run dev:e2e / build:e2e)",
      );
    }
    worker.use(http.get("/api/v1/transactions", () => HttpResponse.json({ items: nextItems })));
  }, items);
}

/**
 * `GET /api/v1/analytics/category` のレスポンスを差し替える。デフォルトハンドラは
 * 空応答を返すため、チャート描画やリスト表示の検証で使う。
 */
export async function mockCategoryAnalytics(
  page: Page,
  payload: CategoryAnalyticsResponse,
): Promise<void> {
  await waitForMswReady(page);
  await page.evaluate((nextPayload) => {
    const worker = window.__mswWorker;
    const http = window.__mswHttp;
    const HttpResponse = window.__mswHttpResponse;
    if (!worker || !http || !HttpResponse) {
      throw new Error(
        "MSW E2E hooks are not exposed on window; run with VITE_ENABLE_MSW=true build (npm run dev:e2e / build:e2e)",
      );
    }
    worker.use(http.get("/api/v1/analytics/category", () => HttpResponse.json(nextPayload)));
  }, payload);
}

/**
 * `GET /api/v1/analytics/need-want` のレスポンスを差し替える。
 */
export async function mockNeedWantAnalytics(
  page: Page,
  payload: NeedWantAnalyticsResponse,
): Promise<void> {
  await waitForMswReady(page);
  await page.evaluate((nextPayload) => {
    const worker = window.__mswWorker;
    const http = window.__mswHttp;
    const HttpResponse = window.__mswHttpResponse;
    if (!worker || !http || !HttpResponse) {
      throw new Error(
        "MSW E2E hooks are not exposed on window; run with VITE_ENABLE_MSW=true build (npm run dev:e2e / build:e2e)",
      );
    }
    worker.use(http.get("/api/v1/analytics/need-want", () => HttpResponse.json(nextPayload)));
  }, payload);
}
