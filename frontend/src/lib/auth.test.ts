import { beforeEach, describe, expect, it } from "vitest";
import { clearToken, getToken, setToken } from "./auth";

describe("auth token store", () => {
  beforeEach(() => {
    clearToken();
  });

  it("returns null when no token is set", () => {
    const result = getToken();

    expect(result).toBeNull();
  });

  it("returns token after setToken is called", () => {
    setToken("jwt-token-123");

    expect(getToken()).toBe("jwt-token-123");
  });

  it("returns null after clearToken is called", () => {
    setToken("jwt-token-123");

    clearToken();

    expect(getToken()).toBeNull();
  });

  it("returns latest token when setToken is called multiple times", () => {
    setToken("first-token");
    setToken("second-token");

    expect(getToken()).toBe("second-token");
  });
});
