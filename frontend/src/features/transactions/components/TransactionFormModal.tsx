/**
 * 支出登録モーダル。
 *
 * shadcn `Dialog` をベースに作成フォームをレンダリングする。
 * 入力済み状態でキャンセル / 背景タップ時は破棄確認を表示する。
 *
 * @see docs/03-design/frontend/screen-flow.md (2a 登録モーダル)
 */

import { useId, useRef, useState, type SyntheticEvent } from "react";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Spinner } from "@/components/ui/spinner";
import { Textarea } from "@/components/ui/textarea";
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";

import { ConfirmDialog } from "../../../components/ConfirmDialog";
import { useCurrency } from "../../../hooks/useCurrency";
import { useToast } from "../../../hooks/useToast";
import { ApiException } from "../../../lib/api/errors";
import { toIsoDate } from "../../../lib/isoDate";
import type { CreateTransactionRequest, NeedWantType } from "../../../types/api";
import { useCategories } from "../../categories/api/useCategories";
import { useCreateTransaction } from "../api/useCreateTransaction";

interface TransactionFormModalProps {
  open: boolean;
  onClose: () => void;
}

interface FormState {
  date: string;
  amount: string;
  categoryId: number | null;
  needWantType: NeedWantType;
  title: string;
  memo: string;
}

function todayIsoDate(): string {
  const now = new Date();
  return toIsoDate(now.getFullYear(), now.getMonth() + 1, now.getDate());
}

function emptyFormState(): FormState {
  return {
    date: todayIsoDate(),
    amount: "",
    categoryId: null,
    needWantType: "UNSET",
    title: "",
    memo: "",
  };
}

function isDirty(state: FormState): boolean {
  return (
    state.amount.length > 0 ||
    state.title.length > 0 ||
    state.memo.length > 0 ||
    state.categoryId !== null ||
    state.needWantType !== "UNSET" ||
    state.date !== todayIsoDate()
  );
}

function validateAmount(raw: string, decimalDigits: number): string | null {
  if (raw.trim().length === 0) return "Amount is required";
  const digitsString = decimalDigits.toString();
  const pattern = decimalDigits === 0 ? /^\d+$/ : new RegExp(`^\\d+(\\.\\d{1,${digitsString}})?$`);
  if (!pattern.test(raw)) {
    return decimalDigits === 0
      ? "Amount must be a whole number"
      : `Amount supports up to ${digitsString} decimal places`;
  }
  if (Number(raw) <= 0) return "Amount must be greater than 0";
  return null;
}

function buildRequest(state: FormState): CreateTransactionRequest {
  const request: CreateTransactionRequest = {
    date: state.date,
    amount: state.amount,
  };
  if (state.categoryId !== null) request.categoryId = state.categoryId;
  if (state.needWantType !== "UNSET") request.needWantType = state.needWantType;
  if (state.title.length > 0) request.title = state.title;
  if (state.memo.length > 0) request.memo = state.memo;
  return request;
}

export function TransactionFormModal({ open, onClose }: TransactionFormModalProps) {
  const dateId = useId();
  const amountId = useId();
  const categoryId = useId();
  const titleId = useId();
  const memoId = useId();

  const [state, setState] = useState<FormState>(emptyFormState);
  const [amountError, setAmountError] = useState<string | null>(null);
  const [showDiscardConfirm, setShowDiscardConfirm] = useState(false);
  const amountRef = useRef<HTMLInputElement>(null);

  const { decimalDigits } = useCurrency();
  const { showSuccess, showError } = useToast();
  const { data: categoriesData } = useCategories();
  const createMutation = useCreateTransaction();

  const resetForm = () => {
    setState(emptyFormState());
    setAmountError(null);
    setShowDiscardConfirm(false);
  };

  const updateField = <K extends keyof FormState>(key: K, value: FormState[K]) => {
    setState((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = (event: SyntheticEvent<HTMLFormElement>) => {
    event.preventDefault();
    const error = validateAmount(state.amount, decimalDigits);
    if (error !== null) {
      setAmountError(error);
      return;
    }
    setAmountError(null);
    createMutation.mutate(buildRequest(state), {
      onSuccess: () => {
        showSuccess("Transaction saved");
        resetForm();
        onClose();
      },
      onError: (err) => {
        const message =
          err instanceof ApiException
            ? err.apiError.detail
            : "Failed to save transaction. Please try again.";
        showError(message);
      },
    });
  };

  const handleAttemptClose = () => {
    if (isDirty(state)) {
      setShowDiscardConfirm(true);
      return;
    }
    resetForm();
    onClose();
  };

  const handleConfirmDiscard = () => {
    resetForm();
    onClose();
  };

  return (
    <>
      <Dialog
        open={open}
        onOpenChange={(nextOpen) => {
          if (!nextOpen) handleAttemptClose();
        }}
      >
        <DialogContent
          onOpenAutoFocus={(event) => {
            event.preventDefault();
            amountRef.current?.focus();
          }}
        >
          <DialogHeader>
            <DialogTitle>Add transaction</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="flex flex-col gap-3">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor={dateId}>Date</Label>
              <Input
                id={dateId}
                type="date"
                value={state.date}
                required
                onChange={(event) => {
                  updateField("date", event.target.value);
                }}
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor={amountId}>Amount</Label>
              <Input
                id={amountId}
                ref={amountRef}
                type="text"
                inputMode="decimal"
                value={state.amount}
                aria-invalid={amountError !== null}
                aria-describedby={amountError !== null ? `${amountId}-error` : undefined}
                onChange={(event) => {
                  updateField("amount", event.target.value);
                  if (amountError !== null) setAmountError(null);
                }}
              />
              {amountError !== null && (
                <p id={`${amountId}-error`} className="text-destructive text-xs">
                  {amountError}
                </p>
              )}
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor={categoryId}>Category</Label>
              <select
                id={categoryId}
                value={state.categoryId ?? ""}
                className="border-input bg-background h-9 rounded-lg border px-2.5 text-sm"
                onChange={(event) => {
                  updateField(
                    "categoryId",
                    event.target.value === "" ? null : Number(event.target.value),
                  );
                }}
              >
                <option value="">Uncategorized</option>
                {categoriesData?.items
                  .filter((category) => category.name !== "Uncategorized")
                  .map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
              </select>
            </div>
            <div className="flex flex-col gap-1.5">
              <span className="text-sm leading-none font-medium">Need / Want</span>
              <ToggleGroup
                type="single"
                value={state.needWantType}
                onValueChange={(next) => {
                  if (next === "NEED" || next === "WANT" || next === "UNSET") {
                    updateField("needWantType", next);
                  }
                }}
                className="self-start"
                rovingFocus={false}
              >
                <ToggleGroupItem value="NEED" aria-label="NEED">
                  NEED
                </ToggleGroupItem>
                <ToggleGroupItem value="WANT" aria-label="WANT">
                  WANT
                </ToggleGroupItem>
                <ToggleGroupItem value="UNSET" aria-label="UNSET">
                  UNSET
                </ToggleGroupItem>
              </ToggleGroup>
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor={titleId}>Title</Label>
              <Input
                id={titleId}
                type="text"
                value={state.title}
                onChange={(event) => {
                  updateField("title", event.target.value);
                }}
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor={memoId}>Memo</Label>
              <Textarea
                id={memoId}
                value={state.memo}
                onChange={(event) => {
                  updateField("memo", event.target.value);
                }}
              />
            </div>
            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={handleAttemptClose}
                disabled={createMutation.isPending}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending ? <Spinner /> : null}
                Save
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
      <ConfirmDialog
        open={showDiscardConfirm}
        title="Discard changes?"
        message="Your unsaved changes will be lost."
        confirmLabel="Discard"
        cancelLabel="Keep editing"
        variant="destructive"
        onConfirm={handleConfirmDiscard}
        onCancel={() => {
          setShowDiscardConfirm(false);
        }}
      />
    </>
  );
}
