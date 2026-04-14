import { ApiErrorSchema } from "../../types/api";
import { clearToken, getToken } from "../auth";
import { ApiException, NetworkException } from "./errors";

const NO_RETRY_STATUSES = new Set([401, 403, 404, 422]);
const MAX_RETRIES = 3;

function toSnakeCaseKey(key: string): string {
  return key
    .replace(/([A-Z]+)([A-Z][a-z])/g, "$1_$2")
    .replace(/([a-z\d])([A-Z])/g, "$1_$2")
    .toLowerCase();
}

function toSnakeCaseBody(value: unknown): unknown {
  if (value === null || value === undefined || typeof value !== "object") {
    return value;
  }
  if (Array.isArray(value)) {
    return value.map(toSnakeCaseBody);
  }
  const result: Record<string, unknown> = {};
  for (const [k, v] of Object.entries(value)) {
    result[toSnakeCaseKey(k)] = toSnakeCaseBody(v);
  }
  return result;
}

async function request(method: string, path: string, body?: unknown): Promise<unknown> {
  const headers: Record<string, string> = {};

  const token = getToken();
  if (token !== null) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const init: RequestInit = { method, headers };

  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
    init.body = JSON.stringify(toSnakeCaseBody(body));
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

async function requestWithRetry(method: string, path: string, body?: unknown): Promise<unknown> {
  for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
    try {
      return await request(method, path, body);
    } catch (error: unknown) {
      const isLastAttempt = attempt === MAX_RETRIES;
      if (isLastAttempt) {
        throw error;
      }

      if (error instanceof ApiException && NO_RETRY_STATUSES.has(error.status)) {
        throw error;
      }

      await new Promise((resolve) => {
        setTimeout(resolve, 2 ** attempt * 1000);
      });
    }
  }

  // unreachable
  throw new Error("Unexpected retry loop exit");
}

export const apiClient = {
  get: (path: string): Promise<unknown> => requestWithRetry("GET", path),
  post: (path: string, body?: unknown): Promise<unknown> => requestWithRetry("POST", path, body),
  put: (path: string, body?: unknown): Promise<unknown> => requestWithRetry("PUT", path, body),
  del: (path: string): Promise<unknown> => requestWithRetry("DELETE", path),
};
