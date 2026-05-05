/**
 * Mutation のエラーをステータスコード別に処理するフック。
 *
 * - 422 + `errors[]` あり → フォームのインラインエラーとして state に保持
 * - 422 で `errors[]` なし → `detail` をトースト
 * - その他の `ApiException` → `detail` をトースト
 * - `NetworkException` / 不明な例外 → 汎用メッセージをトースト
 *
 * 401 の JWT クリアは API クライアント層、ログイン画面リダイレクトは
 * グローバルなエラーハンドラの責務として扱う。
 *
 * @see docs/03-design/common/error-handling.md
 */

import { useCallback, useState } from "react";

import { ApiException, NetworkException } from "../lib/api/errors";
import { extractFieldErrors } from "../lib/api/fieldErrors";
import { useToast } from "./useToast";

interface UseApiErrorReturn {
  fieldErrors: Record<string, string>;
  handleError: (error: unknown) => void;
  clearFieldError: (field: string) => void;
  clearAllFieldErrors: () => void;
}

export function useApiError(): UseApiErrorReturn {
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const { showError } = useToast();

  const handleError = useCallback(
    (error: unknown) => {
      if (error instanceof ApiException) {
        if (error.status === 422) {
          const extracted = extractFieldErrors(error.apiError.errors);
          if (Object.keys(extracted).length > 0) {
            setFieldErrors(extracted);
            return;
          }
        }
        showError(error.apiError.detail);
        return;
      }
      if (error instanceof NetworkException) {
        showError("Network error. Please check your connection.");
        return;
      }
      showError("Something went wrong. Please try again.");
    },
    [showError],
  );

  const clearFieldError = useCallback((field: string) => {
    setFieldErrors((prev) => {
      if (!(field in prev)) return prev;
      const { [field]: _removed, ...rest } = prev;
      return rest;
    });
  }, []);

  const clearAllFieldErrors = useCallback(() => {
    setFieldErrors({});
  }, []);

  return { fieldErrors, handleError, clearFieldError, clearAllFieldErrors };
}
