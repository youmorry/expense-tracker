import { http, HttpResponse } from "msw";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
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
      vi.useFakeTimers();
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          return new HttpResponse("<html>Bad Gateway</html>", {
            status: 502,
            headers: { "Content-Type": "text/html" },
          });
        }),
      );

      const promise = apiClient.get(`${BASE_URL}/test`).catch((e: unknown) => e);
      await vi.advanceTimersByTimeAsync(10_000);
      const error = await promise;

      expect(error).toBeInstanceOf(ApiException);
      if (error instanceof ApiException) {
        expect(error.status).toBe(502);
        expect(error.apiError.type).toBe("about:blank");
      }
      vi.useRealTimers();
    });

    it("throws NetworkException on network failure", async () => {
      vi.useFakeTimers();
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          return HttpResponse.error();
        }),
      );

      const promise = apiClient.get(`${BASE_URL}/test`).catch((e: unknown) => e);
      await vi.advanceTimersByTimeAsync(10_000);
      const error = await promise;

      expect(error).toBeInstanceOf(NetworkException);
      vi.useRealTimers();
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

  describe("retry", () => {
    beforeEach(() => {
      vi.useFakeTimers();
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it("retries on 500 error up to 3 times", async () => {
      let callCount = 0;
      const apiError: ApiError = {
        type: "https://example.com/server-error",
        title: "Internal Server Error",
        status: 500,
        detail: "Server error",
      };
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          callCount++;
          return HttpResponse.json(apiError, { status: 500 });
        }),
      );

      const promise = apiClient.get(`${BASE_URL}/test`).catch(() => {});
      await vi.advanceTimersByTimeAsync(10_000);
      await promise;

      expect(callCount).toBe(4); // 1 initial + 3 retries
    });

    it("returns response when retry succeeds", async () => {
      let callCount = 0;
      const apiError: ApiError = {
        type: "https://example.com/server-error",
        title: "Internal Server Error",
        status: 500,
        detail: "Server error",
      };
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          callCount++;
          if (callCount < 3) {
            return HttpResponse.json(apiError, { status: 500 });
          }
          return HttpResponse.json({ id: 1 });
        }),
      );

      const promise = apiClient.get(`${BASE_URL}/test`);
      await vi.advanceTimersByTimeAsync(10_000);
      const result = await promise;

      expect(result).toEqual({ id: 1 });
      expect(callCount).toBe(3);
    });

    it("does not retry on 401", async () => {
      let callCount = 0;
      const apiError: ApiError = {
        type: "https://example.com/unauthorized",
        title: "Unauthorized",
        status: 401,
        detail: "Invalid token",
      };
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          callCount++;
          return HttpResponse.json(apiError, { status: 401 });
        }),
      );

      await apiClient.get(`${BASE_URL}/test`).catch(() => {});

      expect(callCount).toBe(1);
    });

    it("does not retry on 403", async () => {
      let callCount = 0;
      const apiError: ApiError = {
        type: "https://example.com/forbidden",
        title: "Forbidden",
        status: 403,
        detail: "Access denied",
      };
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          callCount++;
          return HttpResponse.json(apiError, { status: 403 });
        }),
      );

      await apiClient.get(`${BASE_URL}/test`).catch(() => {});

      expect(callCount).toBe(1);
    });

    it("does not retry on 404", async () => {
      let callCount = 0;
      const apiError: ApiError = {
        type: "https://example.com/not-found",
        title: "Not Found",
        status: 404,
        detail: "Not found",
      };
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          callCount++;
          return HttpResponse.json(apiError, { status: 404 });
        }),
      );

      await apiClient.get(`${BASE_URL}/test`).catch(() => {});

      expect(callCount).toBe(1);
    });

    it("does not retry on 400", async () => {
      let callCount = 0;
      const apiError: ApiError = {
        type: "https://example.com/bad-request",
        title: "Bad Request",
        status: 400,
        detail: "Invalid request",
      };
      server.use(
        http.get(`${BASE_URL}/test`, () => {
          callCount++;
          return HttpResponse.json(apiError, { status: 400 });
        }),
      );

      await apiClient.get(`${BASE_URL}/test`).catch(() => {});

      expect(callCount).toBe(1);
    });

    it("does not retry on 422", async () => {
      let callCount = 0;
      const apiError: ApiError = {
        type: "https://example.com/validation-error",
        title: "Unprocessable Entity",
        status: 422,
        detail: "Validation failed",
        errors: [{ detail: "Amount is required", pointer: "#/amount" }],
      };
      server.use(
        http.post(`${BASE_URL}/test`, () => {
          callCount++;
          return HttpResponse.json(apiError, { status: 422 });
        }),
      );

      await apiClient.post(`${BASE_URL}/test`, { amount: "" }).catch(() => {});

      expect(callCount).toBe(1);
    });
  });
});
