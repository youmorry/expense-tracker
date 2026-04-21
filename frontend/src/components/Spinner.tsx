import { Loader2 } from "lucide-react";

interface SpinnerProps {
  label?: string;
}

export function Spinner({ label = "Loading" }: SpinnerProps) {
  return (
    <div role="status" aria-label={label} className="inline-flex items-center justify-center">
      <Loader2 className="h-8 w-8 animate-spin text-gray-500" aria-hidden="true" />
    </div>
  );
}
