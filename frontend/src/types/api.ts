/**
 * API レスポンス・リクエストの Zod スキーマとそこから導出される型定義。
 *
 * レスポンススキーマは API の snake_case キーを受け取り、camelCase に変換する。
 * リクエストスキーマは camelCase で定義し、API クライアント層で snake_case に変換する。
 *
 * @see docs/03-design/backend/api-design.md
 */

import { z } from "zod";

/** 支出の必要度分類 */
const NeedWantTypeSchema = z.enum(["NEED", "WANT", "UNSET"]);
export type NeedWantType = z.infer<typeof NeedWantTypeSchema>;

// ------------------------------------------------------------
// Auth (Response)
// ------------------------------------------------------------

/** ユーザー情報（GET /api/v1/users/me） */
export const UserSchema = z
  .object({
    id: z.number(),
    email: z.string(),
    display_name: z.string(),
    created_at: z.string(),
  })
  .transform(({ display_name, created_at, ...rest }) => ({
    ...rest,
    displayName: display_name,
    createdAt: created_at,
  }));
export type User = z.infer<typeof UserSchema>;

/** Google 認証レスポンス（POST /api/v1/auth/google） */
export const AuthResponseSchema = z
  .object({
    access_token: z.string(),
    user: UserSchema,
  })
  .transform(({ access_token, ...rest }) => ({
    ...rest,
    accessToken: access_token,
  }));
export type AuthResponse = z.infer<typeof AuthResponseSchema>;

// ------------------------------------------------------------
// Auth (Request)
// ------------------------------------------------------------

/** Google 認証リクエスト（POST /api/v1/auth/google） */
export const GoogleAuthRequestSchema = z
  .object({
    id_token: z.string(),
  })
  .transform(({ id_token }) => ({
    idToken: id_token,
  }));
export type GoogleAuthRequest = z.infer<typeof GoogleAuthRequestSchema>;

// ------------------------------------------------------------
// Transaction (Response)
// ------------------------------------------------------------

/** 支出レコード（GET /api/v1/transactions, GET /api/v1/transactions/{id}） */
export const TransactionSchema = z
  .object({
    id: z.number(),
    date: z.string(),
    amount: z.string(),
    category_id: z.number(),
    category_name: z.string(),
    need_want_type: NeedWantTypeSchema,
    title: z.string().optional(),
    memo: z.string().optional(),
    created_at: z.string(),
    updated_at: z.string(),
  })
  .transform(({ category_id, category_name, need_want_type, created_at, updated_at, ...rest }) => ({
    ...rest,
    categoryId: category_id,
    categoryName: category_name,
    needWantType: need_want_type,
    createdAt: created_at,
    updatedAt: updated_at,
  }));
export type Transaction = z.infer<typeof TransactionSchema>;

// ------------------------------------------------------------
// Transaction (Request)
// ------------------------------------------------------------

/** 支出作成リクエスト（POST /api/v1/transactions） */
export const CreateTransactionRequestSchema = z.object({
  date: z.string(),
  amount: z.string(),
  categoryId: z.number().optional(),
  needWantType: NeedWantTypeSchema.optional(),
  title: z.string().optional(),
  memo: z.string().optional(),
});
export type CreateTransactionRequest = z.infer<typeof CreateTransactionRequestSchema>;

/** 支出更新リクエスト（PUT /api/v1/transactions/{id}）。全量更新のため作成と同一形式 */
export type UpdateTransactionRequest = CreateTransactionRequest;

// ------------------------------------------------------------
// Category (Response)
// ------------------------------------------------------------

/** カテゴリ（GET /api/v1/categories） */
export const CategorySchema = z
  .object({
    id: z.number(),
    name: z.string(),
    display_order: z.number(),
  })
  .transform(({ display_order, ...rest }) => ({
    ...rest,
    displayOrder: display_order,
  }));
export type Category = z.infer<typeof CategorySchema>;

// ------------------------------------------------------------
// Analytics (Response)
// ------------------------------------------------------------

/** カテゴリ別集計の個別項目 */
const CategoryAnalyticsItemSchema = z
  .object({
    category_id: z.number(),
    category_name: z.string(),
    amount: z.string(),
    percentage: z.number(),
    transaction_count: z.number(),
  })
  .transform(({ category_id, category_name, transaction_count, ...rest }) => ({
    ...rest,
    categoryId: category_id,
    categoryName: category_name,
    transactionCount: transaction_count,
  }));
export type CategoryAnalyticsItem = z.infer<typeof CategoryAnalyticsItemSchema>;

/** カテゴリ別集計（GET /api/v1/analytics/category） */
export const CategoryAnalyticsSchema = z
  .object({
    total_amount: z.string(),
    categories: z.array(CategoryAnalyticsItemSchema),
  })
  .transform(({ total_amount, ...rest }) => ({
    ...rest,
    totalAmount: total_amount,
  }));
export type CategoryAnalytics = z.infer<typeof CategoryAnalyticsSchema>;

/** Need/Want 集計の個別項目 */
const NeedWantBreakdownItemSchema = z
  .object({
    type: NeedWantTypeSchema,
    amount: z.string(),
    percentage: z.number(),
    transaction_count: z.number(),
  })
  .transform(({ transaction_count, ...rest }) => ({
    ...rest,
    transactionCount: transaction_count,
  }));
export type NeedWantBreakdownItem = z.infer<typeof NeedWantBreakdownItemSchema>;

/** Need/Want 比率（GET /api/v1/analytics/need-want） */
export const NeedWantAnalyticsSchema = z
  .object({
    total_amount: z.string(),
    breakdown: z.array(NeedWantBreakdownItemSchema),
  })
  .transform(({ total_amount, ...rest }) => ({
    ...rest,
    totalAmount: total_amount,
  }));
export type NeedWantAnalytics = z.infer<typeof NeedWantAnalyticsSchema>;

// ------------------------------------------------------------
// Error（RFC 9457 Problem Details）
// ------------------------------------------------------------

/** バリデーションエラーの個別フィールドエラー */
const FieldErrorSchema = z.object({
  detail: z.string(),
  pointer: z.string(),
});
export type FieldError = z.infer<typeof FieldErrorSchema>;

/**
 * API エラーレスポンス（RFC 9457 Problem Details）。
 * @see docs/03-design/common/error-handling.md
 */
export const ApiErrorSchema = z.object({
  type: z.string(),
  title: z.string(),
  status: z.number(),
  detail: z.string(),
  instance: z.string().optional(),
  errors: z.array(FieldErrorSchema).optional(),
});
export type ApiError = z.infer<typeof ApiErrorSchema>;

// ------------------------------------------------------------
// Common
// ------------------------------------------------------------

/** リスト系レスポンスの共通ラッパーを生成する */
export function listResponseSchema<T extends z.ZodType>(itemSchema: T) {
  return z.object({ items: z.array(itemSchema) });
}
