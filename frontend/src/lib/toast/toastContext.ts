import { createContext } from "react";

export type ToastVariant = "success" | "error";

export interface ToastItem {
  id: string;
  variant: ToastVariant;
  message: string;
}

export interface ToastContextValue {
  showSuccess: (message: string) => void;
  showError: (message: string) => void;
}

export const ToastContext = createContext<ToastContextValue | null>(null);
