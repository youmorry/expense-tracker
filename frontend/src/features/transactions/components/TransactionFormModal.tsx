/**
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
import { useApiError } from "../../../hooks/useApiError";
import { useCurrency } from "../../../hooks/useCurrency";
import { useToast } from "../../../hooks/useToast";
import { todayIsoDate } from "../../../lib/isoDate";
import {
  NeedWantTypeSchema,
  type CreateTransactionRequest,
  type NeedWantType,
  type Transaction,
} from "../../../types/api";
import { useCategories } from "../../categories/api/useCategories";
import { useCreateTransaction } from "../api/useCreateTransaction";
import { useDeleteTransaction } from "../api/useDeleteTransaction";
import { useUpdateTransaction } from "../api/useUpdateTransaction";
import { DeleteConfirmDialog } from "./DeleteConfirmDialog";

interface TransactionFormModalProps {
  open: boolean;
  onClose: () => void;
  transaction?: Transaction;
}

interface FormState {
  date: string;
  amount: string;
  categoryId: number | null;
  needWantType: NeedWantType;
  title: string;
  memo: string;
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

function transactionToFormState(transaction: Transaction): FormState {
  return {
    date: transaction.date,
    amount: transaction.amount,
    categoryId: transaction.categoryId,
    needWantType: transaction.needWantType,
    title: transaction.title ?? "",
    memo: transaction.memo ?? "",
  };
}

function isDirty(current: FormState, initial: FormState): boolean {
  return (
    current.date !== initial.date ||
    current.amount !== initial.amount ||
    current.categoryId !== initial.categoryId ||
    current.needWantType !== initial.needWantType ||
    current.title !== initial.title ||
    current.memo !== initial.memo
  );
}

function amountValidationRule(decimalDigits: number): {
  pattern: RegExp;
  tooManyDecimalsMessage: string;
} {
  if (decimalDigits === 0) {
    return { pattern: /^\d+$/, tooManyDecimalsMessage: "Amount must be a whole number" };
  }
  return {
    pattern: new RegExp(`^\\d+(\\.\\d{1,${decimalDigits.toString()}})?$`),
    tooManyDecimalsMessage: `Amount supports up to ${decimalDigits.toString()} decimal places`,
  };
}

function validateAmount(raw: string, decimalDigits: number): string | null {
  if (raw.trim().length === 0) return "Amount is required";
  const { pattern, tooManyDecimalsMessage } = amountValidationRule(decimalDigits);
  if (!pattern.test(raw)) return tooManyDecimalsMessage;
  if (Number(raw) <= 0) return "Amount must be greater than 0";
  return null;
}

function buildRequest(state: FormState): CreateTransactionRequest {
  return {
    date: state.date,
    amount: state.amount,
    ...(state.categoryId !== null && { categoryId: state.categoryId }),
    ...(state.needWantType !== "UNSET" && { needWantType: state.needWantType }),
    ...(state.title.length > 0 && { title: state.title }),
    ...(state.memo.length > 0 && { memo: state.memo }),
  };
}

export function TransactionFormModal({ open, onClose, transaction }: TransactionFormModalProps) {
  const isEditMode = transaction !== undefined;
  const dateId = useId();
  const amountId = useId();
  const categoryFieldId = useId();
  const needWantId = useId();
  const titleId = useId();
  const memoId = useId();

  const [state, setState] = useState<FormState>(() =>
    isEditMode ? transactionToFormState(transaction) : emptyFormState(),
  );
  const [amountError, setAmountError] = useState<string | null>(null);
  const [showDiscardConfirm, setShowDiscardConfirm] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const amountRef = useRef<HTMLInputElement>(null);
  const initialStateRef = useRef<FormState>(state);

  const { decimalDigits } = useCurrency();
  const { showSuccess } = useToast();
  const { fieldErrors, handleError, clearFieldError, clearAllFieldErrors } = useApiError();
  const { data: categoriesData } = useCategories();
  const createMutation = useCreateTransaction();
  const updateMutation = useUpdateTransaction(transaction?.id ?? 0);
  const deleteMutation = useDeleteTransaction();
  const isMutating =
    createMutation.isPending || updateMutation.isPending || deleteMutation.isPending;

  const resetForm = () => {
    setState(emptyFormState());
    setAmountError(null);
    setShowDiscardConfirm(false);
    setShowDeleteConfirm(false);
    clearAllFieldErrors();
  };

  const updateField = <K extends keyof FormState>(key: K, value: FormState[K]) => {
    setState((prev) => ({ ...prev, [key]: value }));
    clearFieldError(key);
  };

  const handleSubmit = (event: SyntheticEvent<HTMLFormElement>) => {
    event.preventDefault();
    const error = validateAmount(state.amount, decimalDigits);
    if (error !== null) {
      setAmountError(error);
      return;
    }
    setAmountError(null);
    clearAllFieldErrors();
    if (isEditMode) {
      updateMutation.mutate(buildRequest(state), {
        onSuccess: () => {
          showSuccess("Transaction updated");
          resetForm();
          onClose();
        },
        onError: handleError,
      });
    } else {
      createMutation.mutate(buildRequest(state), {
        onSuccess: () => {
          showSuccess("Transaction saved");
          resetForm();
          onClose();
        },
        onError: handleError,
      });
    }
  };

  const handleAttemptClose = () => {
    if (isDirty(state, initialStateRef.current)) {
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

  const handleConfirmDelete = () => {
    if (!isEditMode) return;
    deleteMutation.mutate(transaction.id, {
      onSuccess: () => {
        showSuccess("Transaction deleted");
        resetForm();
        onClose();
      },
      onError: (err) => {
        handleError(err);
        setShowDeleteConfirm(false);
      },
    });
  };

  const dateError = fieldErrors.date;
  const amountInlineError = amountError ?? fieldErrors.amount ?? null;
  const categoryError = fieldErrors.categoryId;
  const needWantError = fieldErrors.needWantType;
  const titleError = fieldErrors.title;
  const memoError = fieldErrors.memo;

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
            <DialogTitle>{isEditMode ? "Edit Transaction" : "Add transaction"}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="flex flex-col gap-3">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor={dateId}>Date</Label>
              <Input
                id={dateId}
                type="date"
                value={state.date}
                required
                aria-invalid={dateError !== undefined}
                aria-describedby={dateError !== undefined ? `${dateId}-error` : undefined}
                onChange={(event) => {
                  updateField("date", event.target.value);
                }}
              />
              {dateError !== undefined && (
                <p id={`${dateId}-error`} className="text-destructive text-xs">
                  {dateError}
                </p>
              )}
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor={amountId}>Amount</Label>
              <Input
                id={amountId}
                ref={amountRef}
                type="text"
                inputMode="decimal"
                value={state.amount}
                aria-invalid={amountInlineError !== null}
                aria-describedby={amountInlineError !== null ? `${amountId}-error` : undefined}
                onChange={(event) => {
                  updateField("amount", event.target.value);
                  if (amountError !== null) setAmountError(null);
                }}
              />
              {amountInlineError !== null && (
                <p id={`${amountId}-error`} className="text-destructive text-xs">
                  {amountInlineError}
                </p>
              )}
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor={categoryFieldId}>Category</Label>
              <select
                id={categoryFieldId}
                value={state.categoryId ?? ""}
                aria-invalid={categoryError !== undefined}
                aria-describedby={
                  categoryError !== undefined ? `${categoryFieldId}-error` : undefined
                }
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
              {categoryError !== undefined && (
                <p id={`${categoryFieldId}-error`} className="text-destructive text-xs">
                  {categoryError}
                </p>
              )}
            </div>
            <div className="flex flex-col gap-1.5">
              <span className="text-sm leading-none font-medium">Need / Want</span>
              <ToggleGroup
                type="single"
                value={state.needWantType}
                onValueChange={(next) => {
                  const parsed = NeedWantTypeSchema.safeParse(next);
                  if (parsed.success) updateField("needWantType", parsed.data);
                }}
                className="self-start"
                rovingFocus={false}
              >
                {NeedWantTypeSchema.options.map((value) => (
                  <ToggleGroupItem key={value} value={value} aria-label={value}>
                    {value}
                  </ToggleGroupItem>
                ))}
              </ToggleGroup>
              {needWantError !== undefined && (
                <p id={`${needWantId}-error`} className="text-destructive text-xs">
                  {needWantError}
                </p>
              )}
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor={titleId}>Title</Label>
              <Input
                id={titleId}
                type="text"
                value={state.title}
                aria-invalid={titleError !== undefined}
                aria-describedby={titleError !== undefined ? `${titleId}-error` : undefined}
                onChange={(event) => {
                  updateField("title", event.target.value);
                }}
              />
              {titleError !== undefined && (
                <p id={`${titleId}-error`} className="text-destructive text-xs">
                  {titleError}
                </p>
              )}
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor={memoId}>Memo</Label>
              <Textarea
                id={memoId}
                value={state.memo}
                aria-invalid={memoError !== undefined}
                aria-describedby={memoError !== undefined ? `${memoId}-error` : undefined}
                onChange={(event) => {
                  updateField("memo", event.target.value);
                }}
              />
              {memoError !== undefined && (
                <p id={`${memoId}-error`} className="text-destructive text-xs">
                  {memoError}
                </p>
              )}
            </div>
            <DialogFooter>
              {isEditMode && (
                <Button
                  type="button"
                  variant="destructive"
                  onClick={() => {
                    setShowDeleteConfirm(true);
                  }}
                  disabled={isMutating}
                >
                  Delete
                </Button>
              )}
              <Button
                type="button"
                variant="outline"
                onClick={handleAttemptClose}
                disabled={isMutating}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={isMutating}>
                {createMutation.isPending || updateMutation.isPending ? <Spinner /> : null}
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
      {isEditMode && (
        <DeleteConfirmDialog
          open={showDeleteConfirm}
          transaction={transaction}
          isDeleting={deleteMutation.isPending}
          onConfirm={handleConfirmDelete}
          onCancel={() => {
            setShowDeleteConfirm(false);
          }}
        />
      )}
    </>
  );
}
