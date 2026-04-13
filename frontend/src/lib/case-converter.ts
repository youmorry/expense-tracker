function snakeKeyToCamel(key: string): string {
  return key.replace(/_([a-z])/g, (_, char: string) => char.toUpperCase());
}

function camelKeyToSnake(key: string): string {
  return key.replace(/[A-Z]/g, (char) => `_${char.toLowerCase()}`);
}

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

export function snakeToCamel<T>(obj: T): T {
  return convertKeys(obj, snakeKeyToCamel) as T;
}

export function camelToSnake<T>(obj: T): T {
  return convertKeys(obj, camelKeyToSnake) as T;
}
