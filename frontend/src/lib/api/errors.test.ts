import { describe, expect, it } from "vitest";
import type { ApiError } from "../../types/api";
import { ApiException, NetworkException } from "./errors";

describe("ApiException", () => {
  const apiError: ApiError = {
    type: "https://example.com/not-found",
    title: "Not Found",
    status: 404,
    detail: "The requested resource was not found.",
  };

  it("returns status from constructor argument", () => {
    const exception = new ApiException(404, apiError);

    expect(exception.status).toBe(404);
  });

  it("returns apiError from constructor argument", () => {
    const exception = new ApiException(404, apiError);

    expect(exception.apiError).toBe(apiError);
  });

  it("returns apiError.title as message", () => {
    const exception = new ApiException(404, apiError);

    expect(exception.message).toBe("Not Found");
  });

  it("returns 'ApiException' as name", () => {
    const exception = new ApiException(404, apiError);

    expect(exception.name).toBe("ApiException");
  });

  it("returns true for instanceof Error", () => {
    const exception = new ApiException(404, apiError);

    expect(exception).toBeInstanceOf(Error);
  });
});

describe("NetworkException", () => {
  it("returns 'Network error' as message", () => {
    const exception = new NetworkException(new TypeError("fetch failed"));

    expect(exception.message).toBe("Network error");
  });

  it("returns 'NetworkException' as name", () => {
    const exception = new NetworkException(new TypeError("fetch failed"));

    expect(exception.name).toBe("NetworkException");
  });

  it("returns original error as cause", () => {
    const cause = new TypeError("fetch failed");

    const exception = new NetworkException(cause);

    expect(exception.cause).toBe(cause);
  });

  it("returns true for instanceof Error", () => {
    const exception = new NetworkException(new TypeError("fetch failed"));

    expect(exception).toBeInstanceOf(Error);
  });
});
