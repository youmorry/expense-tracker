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
