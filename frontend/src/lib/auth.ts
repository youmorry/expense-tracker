/**
 * JWT をインメモリ変数で保持する。
 * localStorage / Cookie に保存しないことで XSS によるトークン窃取リスクを最小化する。
 *
 * @see docs/03-design/common/auth-design.md
 */

let token: string | null = null;

export function getToken(): string | null {
  return token;
}

export function setToken(newToken: string): void {
  token = newToken;
}

export function clearToken(): void {
  token = null;
}
