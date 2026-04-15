import { describe, expect, it } from "vitest";
import { ApiException } from "./api/errors";
import { shouldRetryQuery } from "./queryClient";

describe("shouldRetryQuery", () => {
  it("returns false when error is ApiException with 4xx status", () => {
    const error = new ApiException(400, {
      type: "about:blank",
      title: "Bad Request",
      status: 400,
      detail: "Bad Request",
    });

    expect(shouldRetryQuery(0, error)).toBe(false);
  });

  it("returns false when error is ApiException with 401 status", () => {
    const error = new ApiException(401, {
      type: "about:blank",
      title: "Unauthorized",
      status: 401,
      detail: "Unauthorized",
    });

    expect(shouldRetryQuery(0, error)).toBe(false);
  });

  it("returns false when error is ApiException with 422 status", () => {
    const error = new ApiException(422, {
      type: "about:blank",
      title: "Unprocessable Entity",
      status: 422,
      detail: "Validation failed",
    });

    expect(shouldRetryQuery(0, error)).toBe(false);
  });

  it("returns true when error is ApiException with 500 status and failureCount < 3", () => {
    const error = new ApiException(500, {
      type: "about:blank",
      title: "Internal Server Error",
      status: 500,
      detail: "Server error",
    });

    expect(shouldRetryQuery(0, error)).toBe(true);
    expect(shouldRetryQuery(1, error)).toBe(true);
    expect(shouldRetryQuery(2, error)).toBe(true);
  });

  it("returns false when error is ApiException with 500 status and failureCount >= 3", () => {
    const error = new ApiException(500, {
      type: "about:blank",
      title: "Internal Server Error",
      status: 500,
      detail: "Server error",
    });

    expect(shouldRetryQuery(3, error)).toBe(false);
  });

  it("returns true for non-ApiException errors when failureCount < 3", () => {
    const error = new Error("Network error");

    expect(shouldRetryQuery(0, error)).toBe(true);
    expect(shouldRetryQuery(2, error)).toBe(true);
  });

  it("returns false for non-ApiException errors when failureCount >= 3", () => {
    const error = new Error("Network error");

    expect(shouldRetryQuery(3, error)).toBe(false);
  });
});
