import { MutationCache, QueryCache, QueryClient } from "@tanstack/react-query";
import { ApiException } from "./api/errors";

/**
 * Query のリトライ判定関数。
 *
 * 4xx（クライアントエラー）はリクエスト内容の問題のためリトライしない。
 * 5xx（サーバーエラー）やネットワークエラーは最大 3 回リトライする。
 */
export function shouldRetryQuery(failureCount: number, error: Error): boolean {
  if (error instanceof ApiException && error.status < 500) {
    return false;
  }
  return failureCount < 3;
}

// ルーター生成後に main.tsx から登録する。queryClient は React 起動前に
// import されるため、navigate を直接束縛できないことへの対処。
let onUnauthorized: (() => void) | null = null;

export function setOnUnauthorized(handler: () => void): void {
  onUnauthorized = handler;
}

function redirectOnUnauthorized(error: unknown): void {
  if (error instanceof ApiException && error.status === 401) {
    onUnauthorized?.();
  }
}

export const queryClient = new QueryClient({
  queryCache: new QueryCache({ onError: redirectOnUnauthorized }),
  mutationCache: new MutationCache({ onError: redirectOnUnauthorized }),
  defaultOptions: {
    queries: {
      retry: shouldRetryQuery,
    },
    mutations: {
      retry: 0,
    },
  },
});
