/**
 * Playwright の共通ヘルパー。
 *
 * バックエンドは MSW（`src/test/mocks/handlers.ts`）でモックする前提。
 * 個別ケースで応答を変えたいときは `worker.use(...)` を `page.evaluate` 経由で実行する。
 */

import type { http as mswHttp, HttpResponse as MswHttpResponse } from "msw";
import type { SetupWorker } from "msw/browser";

import type { Page } from "@playwright/test";

import type { components } from "../src/types/api-generated";

type TransactionResponse = components["schemas"]["Transaction"];

declare global {
  interface Window {
    __seedAuthToken?: string;
    __mswWorker?: SetupWorker;
    __mswHttp?: typeof mswHttp;
    __mswHttpResponse?: typeof MswHttpResponse;
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
 * `GET /api/v1/transactions` のレスポンスを差し替える。
 *
 * デフォルトハンドラは常に空一覧を返すため、登録 → 一覧反映のような
 * 状態遷移を伴うシナリオではテストごとに `worker.use()` で挙動を上書きする必要がある。
 * `main.tsx` が VITE_ENABLE_MSW=true ビルドで window に公開する MSW ハンドルを使う。
 */
export async function mockTransactionList(page: Page, items: TransactionResponse[]): Promise<void> {
  await page.evaluate((nextItems) => {
    const worker = window.__mswWorker;
    const http = window.__mswHttp;
    const HttpResponse = window.__mswHttpResponse;
    if (!worker || !http || !HttpResponse) {
      throw new Error("MSW worker is not initialized; ensure VITE_ENABLE_MSW=true build is used");
    }
    worker.use(http.get("/api/v1/transactions", () => HttpResponse.json({ items: nextItems })));
  }, items);
}
