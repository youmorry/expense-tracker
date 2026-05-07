/**
 * ブラウザ用 MSW ワーカー。Vite ビルド時に `VITE_ENABLE_MSW=true` の場合のみ
 * `main.tsx` から起動し、Playwright の E2E と手元での開発でバックエンドをモックする。
 *
 * `http` / `HttpResponse` も合わせて re-export する。E2E テストから
 * `window.__mswWorker.use(window.__mswHttp.get(...))` の形で動的にハンドラを差し替えるため。
 */

import { http, HttpResponse } from "msw";
import { setupWorker } from "msw/browser";

import { handlers } from "./handlers";

export { http, HttpResponse };
export const worker = setupWorker(...handlers);
