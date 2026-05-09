/**
 * Playwright の共通ヘルパー。
 *
 * バックエンドは MSW（`src/test/mocks/handlers.ts`）でモックする前提。
 * 個別ケースで応答を変えたいときは `worker.use(...)` を `page.evaluate` 経由で実行する。
 */

import type { QueryClient } from "@tanstack/react-query";
import type { JsonBodyType, http as mswHttp, HttpResponse as MswHttpResponse } from "msw";
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
 * `enableMocking` が非同期で window フックを公開するため、`goto` 直後に
 * `worker.use()` / `queryClient.invalidateQueries()` を呼ぶ前に必ず通す。
 */
async function waitForE2EHooks(page: Page): Promise<void> {
  await page.waitForFunction(
    () =>
      typeof window.__mswWorker !== "undefined" &&
      typeof window.__mswHttp !== "undefined" &&
      typeof window.__mswHttpResponse !== "undefined" &&
      typeof window.__queryClient !== "undefined",
  );
}

const HOOKS_NOT_EXPOSED =
  "E2E hooks are not exposed on window; run with VITE_ENABLE_MSW=true build (npm run dev:e2e / build:e2e)";

/**
 * `worker.use()` だけでは既にキャッシュ済みのレスポンスがそのまま使われるため、
 * モック差し替え後に表示反映させたいときに呼ぶ。
 */
export async function refetchQueries(page: Page): Promise<void> {
  await waitForE2EHooks(page);
  await page.evaluate(async (errorMessage) => {
    const queryClient = window.__queryClient;
    if (!queryClient) {
      throw new Error(errorMessage);
    }
    await queryClient.invalidateQueries();
  }, HOOKS_NOT_EXPOSED);
}

async function mockGet(page: Page, path: string, payload: JsonBodyType): Promise<void> {
  await waitForE2EHooks(page);
  await page.evaluate(
    ({ path, payload, errorMessage }) => {
      const worker = window.__mswWorker;
      const http = window.__mswHttp;
      const HttpResponse = window.__mswHttpResponse;
      if (!worker || !http || !HttpResponse) {
        throw new Error(errorMessage);
      }
      worker.use(http.get(path, () => HttpResponse.json(payload)));
    },
    { path, payload, errorMessage: HOOKS_NOT_EXPOSED },
  );
}

type HttpMethod = "get" | "post" | "put" | "delete";

/**
 * 任意の HTTP メソッド・パス・ステータス・ボディの応答を `worker.use()` で差し替える。
 * 422 / 401 などの異常系シナリオで使う。
 */
export async function mockApiError(
  page: Page,
  method: HttpMethod,
  path: string,
  status: number,
  body: JsonBodyType,
): Promise<void> {
  await waitForE2EHooks(page);
  await page.evaluate(
    ({ method, path, status, body, errorMessage }) => {
      const worker = window.__mswWorker;
      const http = window.__mswHttp;
      const HttpResponse = window.__mswHttpResponse;
      if (!worker || !http || !HttpResponse) {
        throw new Error(errorMessage);
      }
      worker.use(http[method](path, () => HttpResponse.json(body, { status })));
    },
    { method, path, status, body, errorMessage: HOOKS_NOT_EXPOSED },
  );
}

export function mockTransactionList(page: Page, items: TransactionResponse[]): Promise<void> {
  return mockGet(page, "/api/v1/transactions", { items });
}

export function mockCategoryAnalytics(
  page: Page,
  payload: CategoryAnalyticsResponse,
): Promise<void> {
  return mockGet(page, "/api/v1/analytics/category", payload);
}

export function mockNeedWantAnalytics(
  page: Page,
  payload: NeedWantAnalyticsResponse,
): Promise<void> {
  return mockGet(page, "/api/v1/analytics/need-want", payload);
}
