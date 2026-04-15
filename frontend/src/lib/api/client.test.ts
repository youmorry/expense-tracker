/**
 * API クライアントのテスト。
 *
 * MSW を使用してHTTPリクエストをインターセプトし、
 * リクエスト送信・レスポンス解析・エラーハンドリングの動作を検証する。
 */

import { http, HttpResponse } from "msw";
import { afterEach, describe, expect, it } from "vitest";
import type { ApiError } from "../../types/api";
import { server } from "../../test/mocks/server";
import * as auth from "../auth";
import { apiClient } from "./client";
import { ApiException, NetworkException } from "./errors";

const BASE_URL = "/api/v1";

describe("apiClient", () => {
  afterEach(() => {
    auth.clearToken();
  });

  describe("get", () => {
    it("returns JSON response", async () => {
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          return HttpResponse.json({ id: 1, name: "test" });
        }),
      );

      const result = await apiClient.get(`${BASE_URL}/test`);

      expect(result).toEqual({ id: 1, name: "test" });
    });
  });

  describe("post", () => {
    it("sends request body as JSON", async () => {
      let receivedBody: unknown;
      server.use(
        http.post(`${BASE_URL}/test`, async ({ request }) => {
          receivedBody = await request.json();
          return HttpResponse.json({ id: 1 }, { status: 201 });
        }),
      );

      await apiClient.post(`${BASE_URL}/test`, { name: "test" });

      expect(receivedBody).toEqual({ name: "test" });
    });
  });

  describe("put", () => {
    it("sends request body as JSON", async () => {
      let receivedBody: unknown;
      server.use(
        http.put(`${BASE_URL}/test/1`, async ({ request }) => {
          receivedBody = await request.json();
          return HttpResponse.json({ id: 1 });
        }),
      );

      await apiClient.put(`${BASE_URL}/test/1`, { name: "updated" });

      expect(receivedBody).toEqual({ name: "updated" });
    });
  });

  describe("del", () => {
    it("sends DELETE request", async () => {
      let called = false;
      server.use(
        http.delete(`${BASE_URL}/test/1`, () => {
          called = true;
          return new HttpResponse(null, { status: 204 });
        }),
      );

      await apiClient.del(`${BASE_URL}/test/1`);

      expect(called).toBe(true);
    });
  });

  describe("JWT header injection", () => {
    it("adds Authorization header when token exists", async () => {
      auth.setToken("my-jwt-token");
      let authHeader: string | null = null;
      server.use(
        http.get(`${BASE_URL}/test`, ({ request }) => {
          authHeader = request.headers.get("Authorization");
          return HttpResponse.json({});
        }),
      );

      await apiClient.get(`${BASE_URL}/test`);

      expect(authHeader).toBe("Bearer my-jwt-token");
    });

    it("does not add Authorization header when no token", async () => {
      let authHeader: string | null = null;
      server.use(
        http.get(`${BASE_URL}/test`, ({ request }) => {
          authHeader = request.headers.get("Authorization");
          return HttpResponse.json({});
        }),
      );

      await apiClient.get(`${BASE_URL}/test`);

      expect(authHeader).toBeNull();
    });

    it("does not add Authorization header when skipAuth is true", async () => {
      auth.setToken("my-jwt-token");
      let authHeader: string | null = null;
      server.use(
        http.post(`${BASE_URL}/auth/google`, ({ request }) => {
          authHeader = request.headers.get("Authorization");
          return HttpResponse.json({});
        }),
      );

      await apiClient.post(`${BASE_URL}/auth/google`, { idToken: "xxx" }, { skipAuth: true });

      expect(authHeader).toBeNull();
    });
  });

  describe("request body snake_case conversion", () => {
    it("converts camelCase keys to snake_case", async () => {
      let receivedBody: unknown;
      server.use(
        http.post(`${BASE_URL}/test`, async ({ request }) => {
          receivedBody = await request.json();
          return HttpResponse.json({}, { status: 201 });
        }),
      );

      await apiClient.post(`${BASE_URL}/test`, {
        categoryId: 1,
        needWantType: "NEED",
        createdAt: "2025-01-01",
      });

      expect(receivedBody).toEqual({
        category_id: 1,
        need_want_type: "NEED",
        created_at: "2025-01-01",
      });
    });

    it("converts nested camelCase keys to snake_case", async () => {
      let receivedBody: unknown;
      server.use(
        http.post(`${BASE_URL}/test`, async ({ request }) => {
          receivedBody = await request.json();
          return HttpResponse.json({}, { status: 201 });
        }),
      );

      await apiClient.post(`${BASE_URL}/test`, {
        userData: { displayName: "test" },
      });

      expect(receivedBody).toEqual({
        user_data: { display_name: "test" },
      });
    });

    it("converts consecutive uppercase keys to snake_case", async () => {
      let receivedBody: unknown;
      server.use(
        http.post(`${BASE_URL}/test`, async ({ request }) => {
          receivedBody = await request.json();
          return HttpResponse.json({}, { status: 201 });
        }),
      );

      await apiClient.post(`${BASE_URL}/test`, {
        userAPIKey: "abc",
        htmlParser: "v2",
      });

      expect(receivedBody).toEqual({
        user_api_key: "abc",
        html_parser: "v2",
      });
    });
  });

  describe("error handling", () => {
    it("throws ApiException when response is not ok", async () => {
      const apiError: ApiError = {
        type: "https://example.com/not-found",
        title: "Not Found",
        status: 404,
        detail: "Resource not found",
      };
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          return HttpResponse.json(apiError, { status: 404 });
        }),
      );

      const error = await apiClient.get(`${BASE_URL}/test`).catch((e: unknown) => e);

      expect(error).toBeInstanceOf(ApiException);
      if (error instanceof ApiException) {
        expect(error.status).toBe(404);
        expect(error.apiError).toEqual(apiError);
      }
    });

    it("throws ApiException when error response is not JSON", async () => {
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          return new HttpResponse("<html>Bad Gateway</html>", {
            status: 502,
            headers: { "Content-Type": "text/html" },
          });
        }),
      );

      const error = await apiClient.get(`${BASE_URL}/test`).catch((e: unknown) => e);

      expect(error).toBeInstanceOf(ApiException);
      if (error instanceof ApiException) {
        expect(error.status).toBe(502);
        expect(error.apiError.type).toBe("about:blank");
      }
    });

    it("throws NetworkException on network failure", async () => {
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          return HttpResponse.error();
        }),
      );

      const error = await apiClient.get(`${BASE_URL}/test`).catch((e: unknown) => e);

      expect(error).toBeInstanceOf(NetworkException);
    });
  });

  describe("401 handling", () => {
    it("clears token on 401 response", async () => {
      auth.setToken("my-jwt-token");
      const apiError: ApiError = {
        type: "https://example.com/unauthorized",
        title: "Unauthorized",
        status: 401,
        detail: "Invalid token",
      };
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          return HttpResponse.json(apiError, { status: 401 });
        }),
      );

      await apiClient.get(`${BASE_URL}/test`).catch(() => {});

      expect(auth.getToken()).toBeNull();
    });

    it("throws ApiException on 401 response", async () => {
      const apiError: ApiError = {
        type: "https://example.com/unauthorized",
        title: "Unauthorized",
        status: 401,
        detail: "Invalid token",
      };
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          return HttpResponse.json(apiError, { status: 401 });
        }),
      );

      const error = await apiClient.get(`${BASE_URL}/test`).catch((e: unknown) => e);

      expect(error).toBeInstanceOf(ApiException);
      if (error instanceof ApiException) {
        expect(error.status).toBe(401);
      }
    });
  });
});
