import { ChevronRight } from "lucide-react";
import { useId, useState } from "react";

import { ConfirmDialog } from "@/components/ConfirmDialog";
import { Modal } from "@/components/Modal";
import { useCurrency } from "@/hooks/useCurrency";
import { getCurrencySymbol, SUPPORTED_CURRENCIES } from "@/lib/currency";
import { cn } from "@/lib/utils";

export function CurrencySelector() {
  const radioGroupName = useId();
  const { currency, setCurrency } = useCurrency();
  const [isPickerOpen, setIsPickerOpen] = useState(false);
  const [pendingCurrency, setPendingCurrency] = useState<string | null>(null);

  const handleSelect = (next: string): void => {
    if (next === currency) {
      setIsPickerOpen(false);
      return;
    }
    setPendingCurrency(next);
  };

  const handleConfirmChange = (): void => {
    if (pendingCurrency === null) return;
    setCurrency(pendingCurrency);
    setPendingCurrency(null);
    setIsPickerOpen(false);
  };

  return (
    <>
      <button
        type="button"
        onClick={() => {
          setIsPickerOpen(true);
        }}
        className="hover:bg-muted/50 flex w-full items-center justify-between rounded-md py-2 text-left text-sm"
      >
        <span className="font-medium">Currency</span>
        <span className="text-muted-foreground flex items-center gap-1">
          {currency} ({getCurrencySymbol(currency)})
          <ChevronRight className="size-4" />
        </span>
      </button>

      <Modal
        open={isPickerOpen}
        onClose={() => {
          setIsPickerOpen(false);
        }}
        title="Select currency"
      >
        <ul className="flex flex-col">
          {SUPPORTED_CURRENCIES.map((code) => {
            const checked = code === currency;
            return (
              <li key={code}>
                <label
                  className={cn(
                    "hover:bg-muted/50 flex cursor-pointer items-center justify-between rounded-md px-2 py-2 text-sm",
                    checked && "bg-muted/30",
                  )}
                >
                  <span className="flex items-center gap-2">
                    <input
                      type="radio"
                      name={radioGroupName}
                      value={code}
                      checked={checked}
                      readOnly
                      onClick={() => {
                        handleSelect(code);
                      }}
                    />
                    {code} ({getCurrencySymbol(code)})
                  </span>
                </label>
              </li>
            );
          })}
        </ul>
      </Modal>

      <ConfirmDialog
        open={pendingCurrency !== null}
        title="Change display currency?"
        message="Changing currency only affects how amounts are displayed. Existing data will not be converted."
        confirmLabel="Change"
        cancelLabel="Keep current"
        onConfirm={handleConfirmChange}
        onCancel={() => {
          setPendingCurrency(null);
        }}
      />
    </>
  );
}
