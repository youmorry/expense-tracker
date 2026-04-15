/**
 * fetch ベースの API クライアント。
 *
 * 主な機能:
 * - JWT 認証ヘッダーの自動付与（skipAuth オプションで無効化可能）
 * - リクエストボディの camelCase → snake_case キー変換
 * - RFC 9457 Problem Details 形式のエラーレスポンス解析
 * - 401 レスポンス時のトークン自動クリア
 *
 * リトライは TanStack Query の QueryClient が担当する。
 *
 * @see docs/03-design/common/error-handling.md
 * @see docs/03-design/common/auth-design.md
 */

import { ApiErrorSchema } from "../../types/api";
import { clearToken, getToken } from "../auth";
import { toSnakeCaseKeys } from "../caseConverter";
import { ApiException, NetworkException } from "./errors";

/** リクエストごとのオプション */
interface RequestOptions {
  /**
   * true の場合、Authorization ヘッダーを付与しない。
   * 認証不要のエンドポイント（例: POST /api/v1/auth/google）で使用する。
   */
  skipAuth?: boolean;
}

/**
 * HTTP リクエストを実行する。
 * レスポンスの解析とエラーハンドリングを担当する。
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
 * API クライアント。
 *
 * DELETE はリクエストボディを受け付けない（API 設計上の制約）。
 */
export const apiClient = {
  get: (path: string, options?: RequestOptions): Promise<unknown> =>
    request("GET", path, undefined, options),
  post: (path: string, body?: unknown, options?: RequestOptions): Promise<unknown> =>
    request("POST", path, body, options),
  put: (path: string, body?: unknown, options?: RequestOptions): Promise<unknown> =>
    request("PUT", path, body, options),
  /** DELETE はリクエストボディを受け付けない。 */
  del: (path: string, options?: RequestOptions): Promise<unknown> =>
    request("DELETE", path, undefined, options),
};
