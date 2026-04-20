import * as React from "react";
import { Toast as ToastPrimitive } from "radix-ui";
import { cva, type VariantProps } from "class-variance-authority";
import { XIcon } from "lucide-react";

import { cn } from "@/lib/utils";

function ToastProvider({ ...props }: React.ComponentProps<typeof ToastPrimitive.Provider>) {
  return <ToastPrimitive.Provider {...props} />;
}

function ToastViewport({
  className,
  ...props
}: React.ComponentProps<typeof ToastPrimitive.Viewport>) {
  return (
    <ToastPrimitive.Viewport
      data-slot="toast-viewport"
      className={cn(
        "fixed top-0 left-1/2 z-50 flex max-h-screen w-full max-w-sm -translate-x-1/2 flex-col gap-2 p-4 outline-none",
        className,
      )}
      {...props}
    />
  );
}

const toastVariants = cva(
  "data-open:animate-in data-open:slide-in-from-top-full data-closed:animate-out data-closed:fade-out-80 data-closed:slide-out-to-top-full pointer-events-auto relative grid grid-cols-[1fr_auto] items-center gap-3 rounded-md p-3 pr-8 text-sm shadow-lg ring-1 ring-foreground/10",
  {
    variants: {
      variant: {
        success: "bg-popover text-popover-foreground",
        error: "bg-destructive text-white",
      },
    },
    defaultVariants: {
      variant: "success",
    },
  },
);

function Toast({
  className,
  variant,
  ...props
}: React.ComponentProps<typeof ToastPrimitive.Root> & VariantProps<typeof toastVariants>) {
  return (
    <ToastPrimitive.Root
      data-slot="toast"
      data-variant={variant}
      className={cn(toastVariants({ variant }), className)}
      {...props}
    />
  );
}

function ToastTitle({ className, ...props }: React.ComponentProps<typeof ToastPrimitive.Title>) {
  return (
    <ToastPrimitive.Title
      data-slot="toast-title"
      className={cn("text-sm font-medium", className)}
      {...props}
    />
  );
}

function ToastDescription({
  className,
  ...props
}: React.ComponentProps<typeof ToastPrimitive.Description>) {
  return (
    <ToastPrimitive.Description
      data-slot="toast-description"
      className={cn("text-sm", className)}
      {...props}
    />
  );
}

function ToastClose({ className, ...props }: React.ComponentProps<typeof ToastPrimitive.Close>) {
  return (
    <ToastPrimitive.Close
      data-slot="toast-close"
      aria-label="Close"
      className={cn(
        "absolute top-2 right-2 inline-flex size-5 items-center justify-center rounded opacity-70 transition-opacity hover:opacity-100 focus:ring-1 focus:outline-none",
        className,
      )}
      {...props}
    >
      <XIcon className="size-4" />
    </ToastPrimitive.Close>
  );
}

export {
  Toast,
  ToastClose,
  ToastDescription,
  ToastProvider,
  ToastTitle,
  ToastViewport,
  toastVariants,
};
