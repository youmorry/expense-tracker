import { describe, expect, it } from "vitest";
import { toSnakeCaseKeys } from "./caseConverter";

describe("toSnakeCaseKeys", () => {
  it("returns primitive values as-is", () => {
    expect(toSnakeCaseKeys("hello")).toBe("hello");
    expect(toSnakeCaseKeys(42)).toBe(42);
    expect(toSnakeCaseKeys(true)).toBe(true);
    expect(toSnakeCaseKeys(null)).toBeNull();
    expect(toSnakeCaseKeys(undefined)).toBeUndefined();
  });

  it("converts camelCase keys to snake_case", () => {
    const result = toSnakeCaseKeys({
      categoryId: 1,
      needWantType: "NEED",
      createdAt: "2025-01-01",
    });

    expect(result).toEqual({
      category_id: 1,
      need_want_type: "NEED",
      created_at: "2025-01-01",
    });
  });

  it("converts nested object keys recursively", () => {
    const result = toSnakeCaseKeys({
      userData: { displayName: "test" },
    });

    expect(result).toEqual({
      user_data: { display_name: "test" },
    });
  });

  it("converts keys in arrays recursively", () => {
    const result = toSnakeCaseKeys([{ categoryId: 1 }, { categoryId: 2 }]);

    expect(result).toEqual([{ category_id: 1 }, { category_id: 2 }]);
  });

  it("converts consecutive uppercase letters correctly", () => {
    const result = toSnakeCaseKeys({
      userAPIKey: "abc",
      htmlParser: "v2",
    });

    expect(result).toEqual({
      user_api_key: "abc",
      html_parser: "v2",
    });
  });
});
