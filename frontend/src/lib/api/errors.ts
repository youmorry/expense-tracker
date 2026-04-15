/** @see docs/03-design/common/error-handling.md */

import type { ApiError } from "../../types/api";

/**
 * API がエラーレスポンス（4xx / 5xx）を返した場合にスローされる例外。
 * RFC 9457 Problem Details 形式のエラー情報を保持する。
 */
export class ApiException extends Error {
  readonly status: number;
  readonly apiError: ApiError;

  constructor(status: number, apiError: ApiError) {
    super(apiError.title);
    this.name = "ApiException";
    this.status = status;
    this.apiError = apiError;
  }
}

/**
 * ネットワーク障害（DNS 解決失敗、タイムアウト等）でリクエストが完了しなかった場合にスローされる例外。
 * fetch が TypeError を投げるケースに対応する。
 */
export class NetworkException extends Error {
  constructor(cause: unknown) {
    super("Network error");
    this.name = "NetworkException";
    this.cause = cause;
  }
}
