/**
 * フロントエンド（camelCase）とバックエンド API（snake_case）の
 * キー命名規約の差異を吸収するための変換ユーティリティ。
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
