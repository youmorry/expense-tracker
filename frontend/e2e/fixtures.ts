/**
 * Playwright の共通ヘルパー。
 *
 * バックエンドは MSW（`src/test/mocks/handlers.ts`）でモックする前提。
 * 個別ケースで応答を変えたいときは `worker.use(...)` を `page.evaluate` 経由で実行する。
 */

import type { Page } from "@playwright/test";

declare global {
  interface Window {
    __seedAuthToken?: string;
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
