/**
 * API レスポンス・リクエストのグローバル型定義。
 *
 * API の JSON キーは snake_case だが、フロントエンドでは camelCase に変換して使用する。
 * 変換は {@link snakeToCamel} / {@link camelToSnake}（lib/case-converter.ts）で行う。
 *
 * @see docs/03-design/backend/api-design.md
 */

/** 支出の必要度分類 */
export type NeedWantType = "NEED" | "WANT" | "UNSET";

// ------------------------------------------------------------
// Auth
// ------------------------------------------------------------

/** ユーザー情報（GET /api/v1/users/me） */
export interface User {
  id: number;
  email: string;
  displayName: string;
  /** ISO 8601 日時 */
  createdAt: string;
}

/** Google 認証レスポンス（POST /api/v1/auth/google） */
export interface AuthResponse {
  accessToken: string;
  user: User;
}

// ------------------------------------------------------------
// Transaction
// ------------------------------------------------------------

/** 支出レコード（GET /api/v1/transactions, GET /api/v1/transactions/{id}） */
export interface Transaction {
  id: number;
  /** ISO 8601 日付（例: "2026-02-23"） */
  date: string;
  /** 精度保持のため文字列（例: "1200"） */
  amount: string;
  categoryId: number;
  categoryName: string;
  needWantType: NeedWantType;
  title?: string;
  memo?: string;
  /** ISO 8601 日時 */
  createdAt: string;
  /** ISO 8601 日時 */
  updatedAt: string;
}

/** 支出作成リクエスト（POST /api/v1/transactions） */
export interface CreateTransactionRequest {
  date: string;
  /** 精度保持のため文字列 */
  amount: string;
  categoryId?: number;
  needWantType?: NeedWantType;
  title?: string;
  memo?: string;
}

/** 支出更新リクエスト（PUT /api/v1/transactions/{id}）。全量更新のため作成と同一形式 */
export type UpdateTransactionRequest = CreateTransactionRequest;

// ------------------------------------------------------------
// Category
// ------------------------------------------------------------

/** カテゴリ（GET /api/v1/categories） */
export interface Category {
  id: number;
  name: string;
  displayOrder: number;
}

// ------------------------------------------------------------
// Analytics
// ------------------------------------------------------------

/** カテゴリ別集計の個別項目 */
export interface CategoryAnalyticsItem {
  categoryId: number;
  categoryName: string;
  amount: string;
  /** 全体に占める割合（%、小数1桁） */
  percentage: number;
  transactionCount: number;
}

/** カテゴリ別集計（GET /api/v1/analytics/category） */
export interface CategoryAnalytics {
  totalAmount: string;
  categories: CategoryAnalyticsItem[];
}

/** Need/Want 集計の個別項目 */
export interface NeedWantBreakdownItem {
  type: NeedWantType;
  amount: string;
  /** 全体に占める割合（%、小数1桁） */
  percentage: number;
  transactionCount: number;
}

/** Need/Want 比率（GET /api/v1/analytics/need-want） */
export interface NeedWantAnalytics {
  totalAmount: string;
  breakdown: NeedWantBreakdownItem[];
}

// ------------------------------------------------------------
// Error（RFC 9457 Problem Details）
// ------------------------------------------------------------

/** バリデーションエラーの個別フィールドエラー */
export interface FieldError {
  detail: string;
  /** エラー箇所を示す JSON Pointer（例: "#/amount"） */
  pointer: string;
}

/**
 * API エラーレスポンス（RFC 9457 Problem Details）。
 * @see docs/03-design/common/error-handling.md
 */
export interface ApiError {
  /** 問題種別の URI（例: "/errors/validation-error", "about:blank"） */
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  /** バリデーションエラー（422）の場合のみ */
  errors?: FieldError[];
}

// ------------------------------------------------------------
// Common
// ------------------------------------------------------------

/** リスト系レスポンスの共通ラッパー */
export interface ListResponse<T> {
  items: T[];
}
