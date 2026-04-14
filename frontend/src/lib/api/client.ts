/**
 * fetch ベースの API クライアント。
 *
 * 主な機能:
 * - JWT 認証ヘッダーの自動付与（skipAuth オプションで無効化可能）
 * - リクエストボディの camelCase → snake_case キー変換
 * - RFC 9457 Problem Details 形式のエラーレスポンス解析
 * - サーバーエラー・ネットワークエラー時の指数バックオフリトライ（最大3回）
 * - 401 レスポンス時のトークン自動クリア
 *
 * @see docs/03-design/common/error-handling.md
 * @see docs/03-design/common/auth-design.md
 */

import { ApiErrorSchema } from "../../types/api";
import { clearToken, getToken } from "../auth";
import { toSnakeCaseKeys } from "../caseConverter";
import { ApiException, NetworkException } from "./errors";

/**
 * リトライ対象外のステータスコード。
 * クライアント起因のエラーはリトライしても結果が変わらないため除外する。
 */
const NO_RETRY_STATUSES = new Set([400, 401, 403, 404, 422]);
const MAX_RETRIES = 3;

/** リクエストごとのオプション */
interface RequestOptions {
  /**
   * true の場合、Authorization ヘッダーを付与しない。
   * 認証不要のエンドポイント（例: POST /api/v1/auth/google）で使用する。
   */
  skipAuth?: boolean;
}

/**
 * 単一の HTTP リクエストを実行する。
 * リトライは行わず、レスポンスの解析とエラーハンドリングを担当する。
 */
async function request(
  method: string,
  path: string,
  body?: unknown,
  options?: RequestOptions,
): Promise<unknown> {
  const headers: Record<string, string> = {};

  if (!options?.skipAuth) {
    const token = getToken();
    if (token !== null) {
      headers["Authorization"] = `Bearer ${token}`;
    }
  }

  const init: RequestInit = { method, headers };

  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
    init.body = JSON.stringify(toSnakeCaseKeys(body));
  }

  let response: Response;
  try {
    response = await fetch(path, init);
  } catch (error: unknown) {
    throw new NetworkException(error);
  }

  if (!response.ok) {
    let apiError;
    try {
      const errorBody: unknown = await response.json();
      apiError = ApiErrorSchema.parse(errorBody);
    } catch {
      throw new ApiException(response.status, {
        type: "about:blank",
        title: response.statusText,
        status: response.status,
        detail: response.statusText,
      });
    }

    if (response.status === 401) {
      clearToken();
    }

    throw new ApiException(response.status, apiError);
  }

  if (response.status === 204) {
    return undefined;
  }

  const json: unknown = await response.json();
  return json;
}

/**
 * リトライ付きで HTTP リクエストを実行する。
 * サーバーエラー（5xx）やネットワークエラーは指数バックオフで最大 {@link MAX_RETRIES} 回リトライする。
 * {@link NO_RETRY_STATUSES} に含まれるステータスコードはリトライしない。
 */
async function requestWithRetry(
  method: string,
  path: string,
  body?: unknown,
  options?: RequestOptions,
): Promise<unknown> {
  for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
    try {
      return await request(method, path, body, options);
    } catch (error: unknown) {
      const isLastAttempt = attempt === MAX_RETRIES;
      if (isLastAttempt) {
        throw error;
      }

      if (error instanceof ApiException && NO_RETRY_STATUSES.has(error.status)) {
        throw error;
      }

      await new Promise((resolve) => {
        setTimeout(resolve, 2 ** attempt * 1000);
      });
    }
  }

  // unreachable
  throw new Error("Unexpected retry loop exit");
}

/**
 * API クライアント。
 *
 * 各メソッドはリトライ付きでリクエストを実行する。
 * DELETE はリクエストボディを受け付けない（API 設計上の制約）。
 */
export const apiClient = {
  get: (path: string, options?: RequestOptions): Promise<unknown> =>
    requestWithRetry("GET", path, undefined, options),
  post: (path: string, body?: unknown, options?: RequestOptions): Promise<unknown> =>
    requestWithRetry("POST", path, body, options),
  put: (path: string, body?: unknown, options?: RequestOptions): Promise<unknown> =>
    requestWithRetry("PUT", path, body, options),
  /** DELETE はリクエストボディを受け付けない。 */
  del: (path: string, options?: RequestOptions): Promise<unknown> =>
    requestWithRetry("DELETE", path, undefined, options),
};
