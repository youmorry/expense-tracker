import { describe, expect, it } from "vitest";
import { camelToSnake, snakeToCamel } from "./case-converter";

describe("snakeToCamel", () => {
  it("returns primitive values as-is", () => {
    expect(snakeToCamel("hello")).toBe("hello");
    expect(snakeToCamel(42)).toBe(42);
    expect(snakeToCamel(true)).toBe(true);
    expect(snakeToCamel(null)).toBeNull();
    expect(snakeToCamel(undefined)).toBeUndefined();
  });

  it("converts flat object keys from snake_case to camelCase", () => {
    const input = { user_id: 1, display_name: "Yuto", created_at: "2026-01-01T00:00:00Z" };

    const result = snakeToCamel(input);

    expect(result).toEqual({ userId: 1, displayName: "Yuto", createdAt: "2026-01-01T00:00:00Z" });
  });

  it("converts nested object keys recursively", () => {
    const input = { access_token: "jwt", user: { display_name: "Yuto", created_at: "2026-01-01" } };

    const result = snakeToCamel(input);

    expect(result).toEqual({
      accessToken: "jwt",
      user: { displayName: "Yuto", createdAt: "2026-01-01" },
    });
  });

  it("converts objects inside arrays", () => {
    const input = { items: [{ category_id: 1, category_name: "Food" }] };

    const result = snakeToCamel(input);

    expect(result).toEqual({ items: [{ categoryId: 1, categoryName: "Food" }] });
  });

  it("returns empty object and empty array as-is", () => {
    expect(snakeToCamel({})).toEqual({});
    expect(snakeToCamel([])).toEqual([]);
  });

  it("keeps single-word keys unchanged", () => {
    const input = { id: 1, name: "Food", email: "test@example.com" };

    const result = snakeToCamel(input);

    expect(result).toEqual({ id: 1, name: "Food", email: "test@example.com" });
  });
});

describe("camelToSnake", () => {
  it("returns primitive values as-is", () => {
    expect(camelToSnake("hello")).toBe("hello");
    expect(camelToSnake(42)).toBe(42);
    expect(camelToSnake(true)).toBe(true);
    expect(camelToSnake(null)).toBeNull();
    expect(camelToSnake(undefined)).toBeUndefined();
  });

  it("converts flat object keys from camelCase to snake_case", () => {
    const input = { userId: 1, displayName: "Yuto", createdAt: "2026-01-01T00:00:00Z" };

    const result = camelToSnake(input);

    expect(result).toEqual({
      user_id: 1,
      display_name: "Yuto",
      created_at: "2026-01-01T00:00:00Z",
    });
  });

  it("converts nested object keys recursively", () => {
    const input = {
      accessToken: "jwt",
      user: { displayName: "Yuto", createdAt: "2026-01-01" },
    };

    const result = camelToSnake(input);

    expect(result).toEqual({
      access_token: "jwt",
      user: { display_name: "Yuto", created_at: "2026-01-01" },
    });
  });

  it("converts objects inside arrays", () => {
    const input = { items: [{ categoryId: 1, categoryName: "Food" }] };

    const result = camelToSnake(input);

    expect(result).toEqual({ items: [{ category_id: 1, category_name: "Food" }] });
  });

  it("returns empty object and empty array as-is", () => {
    expect(camelToSnake({})).toEqual({});
    expect(camelToSnake([])).toEqual([]);
  });

  it("keeps single-word keys unchanged", () => {
    const input = { id: 1, name: "Food", email: "test@example.com" };

    const result = camelToSnake(input);

    expect(result).toEqual({ id: 1, name: "Food", email: "test@example.com" });
  });
});
