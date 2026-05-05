import { describe, expect, it } from "vitest";

import type { FieldError } from "../../types/api";
import { extractFieldErrors, pointerToFieldName } from "./fieldErrors";

describe("pointerToFieldName", () => {
  it("returns the field name for a single-token pointer", () => {
    expect(pointerToFieldName("#/amount")).toBe("amount");
  });

  it("converts snake_case pointer to camelCase field name", () => {
    expect(pointerToFieldName("#/category_id")).toBe("categoryId");
  });

  it("converts multi-segment snake_case pointer to camelCase", () => {
    expect(pointerToFieldName("#/need_want_type")).toBe("needWantType");
  });

  it("returns null when pointer does not start with the JSON pointer prefix", () => {
    expect(pointerToFieldName("amount")).toBeNull();
  });

  it("returns null for an empty pointer", () => {
    expect(pointerToFieldName("")).toBeNull();
  });

  it("returns null when pointer body is empty", () => {
    expect(pointerToFieldName("#/")).toBeNull();
  });
});

describe("extractFieldErrors", () => {
  it("returns an empty record when errors is undefined", () => {
    expect(extractFieldErrors(undefined)).toEqual({});
  });

  it("returns an empty record when errors is an empty array", () => {
    expect(extractFieldErrors([])).toEqual({});
  });

  it("maps each pointer to its detail message keyed by camelCase field name", () => {
    const errors: FieldError[] = [
      { pointer: "#/amount", detail: "must be greater than 0" },
      { pointer: "#/category_id", detail: "category not found" },
    ];

    expect(extractFieldErrors(errors)).toEqual({
      amount: "must be greater than 0",
      categoryId: "category not found",
    });
  });

  it("keeps the first message when multiple errors target the same field", () => {
    const errors: FieldError[] = [
      { pointer: "#/amount", detail: "must be greater than 0" },
      { pointer: "#/amount", detail: "must not be null" },
    ];

    expect(extractFieldErrors(errors)).toEqual({
      amount: "must be greater than 0",
    });
  });

  it("skips entries whose pointer cannot be resolved to a field name", () => {
    const errors: FieldError[] = [
      { pointer: "amount", detail: "ignored" },
      { pointer: "#/amount", detail: "must be greater than 0" },
    ];

    expect(extractFieldErrors(errors)).toEqual({
      amount: "must be greater than 0",
    });
  });
});
