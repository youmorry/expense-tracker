import type { ReactNode } from "react";

interface EmptyStateProps {
  title: string;
  description?: string;
  icon?: ReactNode;
  action?: ReactNode;
}

export function EmptyState({ title, description, icon, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 px-6 py-12 text-center">
      {icon && <div className="text-muted-foreground">{icon}</div>}
      <p className="text-foreground text-base font-medium">{title}</p>
      {description && <p className="text-muted-foreground text-sm">{description}</p>}
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
}
