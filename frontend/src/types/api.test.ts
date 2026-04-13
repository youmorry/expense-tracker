import { describe, expect, it } from "vitest";
import {
  ApiErrorSchema,
  AuthResponseSchema,
  CategoryAnalyticsSchema,
  CategorySchema,
  GoogleAuthRequestSchema,
  NeedWantAnalyticsSchema,
  TransactionSchema,
  UserSchema,
  type CreateTransactionRequest,
  type UpdateTransactionRequest,
} from "./api";

describe("UserSchema", () => {
  it("transforms snake_case keys to camelCase", () => {
    const input = { id: 1, email: "user@example.com", display_name: "Yuto", created_at: "2026-01-01T00:00:00Z" };

    const result = UserSchema.parse(input);

    expect(result).toEqual({
      id: 1,
      email: "user@example.com",
      displayName: "Yuto",
      createdAt: "2026-01-01T00:00:00Z",
    });
  });

  it("rejects input with missing required field", () => {
    const input = { id: 1, email: "user@example.com" };

    expect(() => UserSchema.parse(input)).toThrow();
  });
});

describe("AuthResponseSchema", () => {
  it("transforms nested user and access_token", () => {
    const input = {
      access_token: "jwt-token",
      user: { id: 1, email: "user@example.com", display_name: "Yuto", created_at: "2026-01-01T00:00:00Z" },
    };

    const result = AuthResponseSchema.parse(input);

    expect(result).toEqual({
      accessToken: "jwt-token",
      user: {
        id: 1,
        email: "user@example.com",
        displayName: "Yuto",
        createdAt: "2026-01-01T00:00:00Z",
      },
    });
  });
});

describe("GoogleAuthRequestSchema", () => {
  it("validates id_token field", () => {
    const input = { id_token: "google-id-token" };

    const result = GoogleAuthRequestSchema.parse(input);

    expect(result).toEqual({ idToken: "google-id-token" });
  });
});

describe("TransactionSchema", () => {
  it("transforms all snake_case keys to camelCase", () => {
    const input = {
      id: 42,
      date: "2026-02-23",
      amount: "1200",
      category_id: 1,
      category_name: "Food",
      need_want_type: "NEED",
      title: "Lunch",
      memo: "Company cafeteria",
      created_at: "2026-02-23T10:30:00Z",
      updated_at: "2026-02-23T10:30:00Z",
    };

    const result = TransactionSchema.parse(input);

    expect(result).toEqual({
      id: 42,
      date: "2026-02-23",
      amount: "1200",
      categoryId: 1,
      categoryName: "Food",
      needWantType: "NEED",
      title: "Lunch",
      memo: "Company cafeteria",
      createdAt: "2026-02-23T10:30:00Z",
      updatedAt: "2026-02-23T10:30:00Z",
    });
  });

  it("allows optional title and memo to be absent", () => {
    const input = {
      id: 42,
      date: "2026-02-23",
      amount: "1200",
      category_id: 1,
      category_name: "Food",
      need_want_type: "UNSET",
      created_at: "2026-02-23T10:30:00Z",
      updated_at: "2026-02-23T10:30:00Z",
    };

    const result = TransactionSchema.parse(input);

    expect(result.title).toBeUndefined();
    expect(result.memo).toBeUndefined();
  });

  it("rejects invalid need_want_type value", () => {
    const input = {
      id: 42,
      date: "2026-02-23",
      amount: "1200",
      category_id: 1,
      category_name: "Food",
      need_want_type: "INVALID",
      created_at: "2026-02-23T10:30:00Z",
      updated_at: "2026-02-23T10:30:00Z",
    };

    expect(() => TransactionSchema.parse(input)).toThrow();
  });
});

describe("CategorySchema", () => {
  it("transforms display_order to displayOrder", () => {
    const input = { id: 1, name: "Food", display_order: 1 };

    const result = CategorySchema.parse(input);

    expect(result).toEqual({ id: 1, name: "Food", displayOrder: 1 });
  });
});

describe("CategoryAnalyticsSchema", () => {
  it("transforms nested category items", () => {
    const input = {
      total_amount: "130000",
      categories: [
        {
          category_id: 1,
          category_name: "Food",
          amount: "45000",
          percentage: 34.6,
          transaction_count: 28,
        },
      ],
    };

    const result = CategoryAnalyticsSchema.parse(input);

    expect(result).toEqual({
      totalAmount: "130000",
      categories: [
        {
          categoryId: 1,
          categoryName: "Food",
          amount: "45000",
          percentage: 34.6,
          transactionCount: 28,
        },
      ],
    });
  });
});

describe("NeedWantAnalyticsSchema", () => {
  it("transforms nested breakdown items", () => {
    const input = {
      total_amount: "130000",
      breakdown: [
        { type: "NEED", amount: "80000", percentage: 61.5, transaction_count: 45 },
        { type: "WANT", amount: "35000", percentage: 26.9, transaction_count: 12 },
        { type: "UNSET", amount: "15000", percentage: 11.5, transaction_count: 3 },
      ],
    };

    const result = NeedWantAnalyticsSchema.parse(input);

    expect(result).toEqual({
      totalAmount: "130000",
      breakdown: [
        { type: "NEED", amount: "80000", percentage: 61.5, transactionCount: 45 },
        { type: "WANT", amount: "35000", percentage: 26.9, transactionCount: 12 },
        { type: "UNSET", amount: "15000", percentage: 11.5, transactionCount: 3 },
      ],
    });
  });
});

describe("ApiErrorSchema", () => {
  it("transforms with validation errors", () => {
    const input = {
      type: "/errors/validation-error",
      title: "Your request is not valid.",
      status: 422,
      detail: "One or more fields have validation errors.",
      instance: "/api/v1/transactions",
      errors: [{ detail: "must not be null", pointer: "#/date" }],
    };

    const result = ApiErrorSchema.parse(input);

    expect(result).toEqual(input);
  });

  it("allows optional instance and errors to be absent", () => {
    const input = {
      type: "about:blank",
      title: "Not Found",
      status: 404,
      detail: "The requested transaction was not found.",
    };

    const result = ApiErrorSchema.parse(input);

    expect(result.instance).toBeUndefined();
    expect(result.errors).toBeUndefined();
  });
});

describe("Request types", () => {
  it("CreateTransactionRequest type accepts valid camelCase input", () => {
    const request: CreateTransactionRequest = {
      date: "2026-02-23",
      amount: "1200",
      categoryId: 1,
      needWantType: "NEED",
      title: "Lunch",
    };

    expect(request.date).toBe("2026-02-23");
  });

  it("UpdateTransactionRequest is same type as CreateTransactionRequest", () => {
    const create: CreateTransactionRequest = { date: "2026-02-23", amount: "1200" };
    const update: UpdateTransactionRequest = create;

    expect(update).toBe(create);
  });
});
