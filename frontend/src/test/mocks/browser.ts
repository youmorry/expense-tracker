/**
 * ブラウザ用 MSW ワーカー。Vite ビルド時に `VITE_ENABLE_MSW=true` の場合のみ
 * `main.tsx` から起動し、Playwright の E2E と手元での開発でバックエンドをモックする。
 */

import { setupWorker } from "msw/browser";

import { handlers } from "./handlers";

export const worker = setupWorker(...handlers);
