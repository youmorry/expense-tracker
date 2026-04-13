export type NeedWantType = "NEED" | "WANT" | "UNSET";

export interface User {
  id: number;
  email: string;
  displayName: string;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  user: User;
}

export interface Transaction {
  id: number;
  date: string;
  amount: string;
  categoryId: number;
  categoryName: string;
  needWantType: NeedWantType;
  title?: string;
  memo?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTransactionRequest {
  date: string;
  amount: string;
  categoryId?: number;
  needWantType?: NeedWantType;
  title?: string;
  memo?: string;
}

export type UpdateTransactionRequest = CreateTransactionRequest;

export interface Category {
  id: number;
  name: string;
  displayOrder: number;
}

export interface CategoryAnalyticsItem {
  categoryId: number;
  categoryName: string;
  amount: string;
  percentage: number;
  transactionCount: number;
}

export interface CategoryAnalytics {
  totalAmount: string;
  categories: CategoryAnalyticsItem[];
}

export interface NeedWantBreakdownItem {
  type: NeedWantType;
  amount: string;
  percentage: number;
  transactionCount: number;
}

export interface NeedWantAnalytics {
  totalAmount: string;
  breakdown: NeedWantBreakdownItem[];
}

export interface FieldError {
  detail: string;
  pointer: string;
}

export interface ApiError {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  errors?: FieldError[];
}

export interface ListResponse<T> {
  items: T[];
}
