/**
 * JSON オブジェクトのキーを snake_case ↔ camelCase で相互変換するユーティリティ。
 *
 * API の JSON キーは snake_case、フロントエンドは camelCase を使うため、
 * レスポンス受信時に {@link snakeToCamel}、リクエスト送信時に {@link camelToSnake} で変換する。
 *
 * プリミティブ値・null・undefined はそのまま返し、配列・ネストオブジェクトは再帰的に変換する。
 */

function snakeKeyToCamel(key: string): string {
  return key.replace(/_([a-z])/g, (_, char: string) => char.toUpperCase());
}

function camelKeyToSnake(key: string): string {
  return key.replace(/[A-Z]/g, (char) => `_${char.toLowerCase()}`);
}

/** オブジェクトのキーを再帰的に変換する。配列内のオブジェクトも対象。 */
function convertKeys(obj: unknown, keyConverter: (key: string) => string): unknown {
  if (obj === null || obj === undefined || typeof obj !== "object") {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map((item) => convertKeys(item, keyConverter));
  }

  const result: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(obj)) {
    result[keyConverter(key)] = convertKeys(value, keyConverter);
  }
  return result;
}

/** API レスポンスの snake_case キーを camelCase に変換する */
export function snakeToCamel<T>(obj: T): T {
  return convertKeys(obj, snakeKeyToCamel) as T;
}

/** リクエスト送信用に camelCase キーを snake_case に変換する */
export function camelToSnake<T>(obj: T): T {
  return convertKeys(obj, camelKeyToSnake) as T;
}
