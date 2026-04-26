import { useCallback, useMemo, useState, type ReactNode } from "react";

import {
  Toast,
  ToastClose,
  ToastDescription,
  ToastProvider as ToastPrimitiveProvider,
  ToastViewport,
} from "@/components/ui/toast";
import { ToastContext, type ToastContextValue, type ToastItem } from "@/lib/toast/toastContext";

const SUCCESS_DURATION_MS = 3000;

interface ToastProviderProps {
  children: ReactNode;
}

export function ToastProvider({ children }: ToastProviderProps) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const dismiss = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const push = useCallback((variant: ToastItem["variant"], message: string) => {
    setToasts((prev) => [...prev, { id: crypto.randomUUID(), variant, message }]);
  }, []);

  const showSuccess = useCallback(
    (message: string) => {
      push("success", message);
    },
    [push],
  );
  const showError = useCallback(
    (message: string) => {
      push("error", message);
    },
    [push],
  );

  const value = useMemo<ToastContextValue>(
    () => ({ showSuccess, showError }),
    [showSuccess, showError],
  );

  return (
    <ToastContext.Provider value={value}>
      <ToastPrimitiveProvider label="Notifications" swipeDirection="up">
        {children}
        {toasts.map((t) => (
          <Toast
            key={t.id}
            variant={t.variant}
            duration={t.variant === "success" ? SUCCESS_DURATION_MS : Infinity}
            onOpenChange={(open) => {
              if (!open) dismiss(t.id);
            }}
          >
            <ToastDescription>{t.message}</ToastDescription>
            {t.variant === "error" && <ToastClose />}
          </Toast>
        ))}
        <ToastViewport />
      </ToastPrimitiveProvider>
    </ToastContext.Provider>
  );
}
