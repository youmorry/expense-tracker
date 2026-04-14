/**
 * オブジェクトのキーを camelCase から snake_case に再帰的に変換するユーティリティ。
 *
 * API リクエストボディの送信時に使用する。
 * フロントエンドは camelCase、バックエンド API は snake_case を採用しているため、
 * リクエスト時にキーを変換する必要がある。
 *
 * @see docs/03-design/backend/api-design.md
 */

/**
 * camelCase 文字列を snake_case に変換する。
 *
 * 連続する大文字にも対応する（例: `userAPIKey` → `user_api_key`）。
 */
function toSnakeCaseKey(key: string): string {
  return key
    .replace(/([A-Z]+)([A-Z][a-z])/g, "$1_$2")
    .replace(/([a-z\d])([A-Z])/g, "$1_$2")
    .toLowerCase();
}

/**
 * オブジェクトのキーを再帰的に snake_case に変換する。
 *
 * - プリミティブ値はそのまま返す
 * - 配列は各要素を再帰的に変換する
 * - オブジェクトはキーを snake_case に変換し、値を再帰的に変換する
 */
export function toSnakeCaseKeys(value: unknown): unknown {
  if (value === null || value === undefined || typeof value !== "object") {
    return value;
  }
  if (Array.isArray(value)) {
    return value.map(toSnakeCaseKeys);
  }
  const result: Record<string, unknown> = {};
  for (const [k, v] of Object.entries(value)) {
    result[toSnakeCaseKey(k)] = toSnakeCaseKeys(v);
  }
  return result;
}
