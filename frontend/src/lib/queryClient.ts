import { QueryClient } from "@tanstack/react-query";
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

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: shouldRetryQuery,
    },
    mutations: {
      retry: 0,
    },
  },
});
