/**
 * ブラウザ用 MSW ワーカー。Vite ビルド時に `VITE_ENABLE_MSW=true` の場合のみ
 * `main.tsx` から起動し、Playwright の E2E と手元での開発でバックエンドをモックする。
 */

import { http, HttpResponse } from "msw";
import { setupWorker } from "msw/browser";

import { handlers } from "./handlers";

// E2E から worker.use() を組み立てるため http / HttpResponse も再公開する。
export { http, HttpResponse };
export const worker = setupWorker(...handlers);
