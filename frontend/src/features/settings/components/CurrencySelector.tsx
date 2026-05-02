import { ChevronRight } from "lucide-react";
import { useId, useState } from "react";

import { ConfirmDialog } from "@/components/ConfirmDialog";
import { Modal } from "@/components/Modal";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { useCurrency } from "@/hooks/useCurrency";
import { getCurrencySymbol, SUPPORTED_CURRENCIES } from "@/lib/currency";
import { cn } from "@/lib/utils";

export function CurrencySelector() {
  const idPrefix = useId();
  const { currency, setCurrency } = useCurrency();
  const [isPickerOpen, setIsPickerOpen] = useState(false);
  const [pendingCurrency, setPendingCurrency] = useState<string | null>(null);

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
        <RadioGroup
          value={currency}
          onValueChange={(next) => {
            setPendingCurrency(next);
          }}
        >
          {SUPPORTED_CURRENCIES.map((code) => {
            const checked = code === currency;
            const id = `${idPrefix}-${code}`;
            return (
              <Label
                key={code}
                htmlFor={id}
                className={cn(
                  "hover:bg-muted/50 cursor-pointer rounded-md px-2 py-2 font-normal",
                  checked && "bg-muted/30",
                )}
              >
                <RadioGroupItem
                  value={code}
                  id={id}
                  onClick={() => {
                    if (checked) setIsPickerOpen(false);
                  }}
                />
                <span>
                  {code} ({getCurrencySymbol(code)})
                </span>
              </Label>
            );
          })}
        </RadioGroup>
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
