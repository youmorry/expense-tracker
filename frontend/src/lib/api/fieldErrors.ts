/**
 * RFC 9457 Problem Details の `errors[]` 配列に含まれる JSON Pointer を
 * フォーム側の camelCase フィールド名に変換するユーティリティ。
 *
 * バックエンドはフィールド名を snake_case で表現するため
 * (`#/category_id`)、フォーム側のキー (`categoryId`) と突合できるよう
 * ここで変換する。
 *
 * @see docs/03-design/common/error-handling.md
 */

import type { FieldError } from "../../types/api";

const POINTER_PREFIX = "#/";

export function pointerToFieldName(pointer: string): string | null {
  if (!pointer.startsWith(POINTER_PREFIX)) return null;
  const body = pointer.slice(POINTER_PREFIX.length);
  if (body.length === 0) return null;
  return body.replace(/_([a-z])/g, (_, char: string) => char.toUpperCase());
}

export function extractFieldErrors(errors: FieldError[] | undefined): Record<string, string> {
  const result: Record<string, string> = {};
  if (errors === undefined) return result;
  for (const { pointer, detail } of errors) {
    const field = pointerToFieldName(pointer);
    if (field === null) continue;
    // 同一フィールドに複数の制約違反がある場合は最初のメッセージのみ採用
    if (field in result) continue;
    result[field] = detail;
  }
  return result;
}
