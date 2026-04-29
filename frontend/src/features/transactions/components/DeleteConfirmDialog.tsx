import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

import { useCurrency } from "../../../hooks/useCurrency";
import type { Transaction } from "../../../types/api";

interface DeleteConfirmDialogProps {
  open: boolean;
  transaction: Transaction;
  isDeleting: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeleteConfirmDialog({
  open,
  transaction,
  isDeleting,
  onConfirm,
  onCancel,
}: DeleteConfirmDialogProps) {
  const { formatAmount } = useCurrency();

  return (
    <AlertDialog open={open}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Delete this transaction?</AlertDialogTitle>
        </AlertDialogHeader>
        <dl className="text-sm">
          <div className="flex justify-between py-1">
            <dt className="text-muted-foreground">Date</dt>
            <dd>{transaction.date}</dd>
          </div>
          <div className="flex justify-between py-1">
            <dt className="text-muted-foreground">Amount</dt>
            <dd>{formatAmount(Number(transaction.amount))}</dd>
          </div>
          <div className="flex justify-between py-1">
            <dt className="text-muted-foreground">Category</dt>
            <dd>{transaction.categoryName}</dd>
          </div>
        </dl>
        <AlertDialogFooter>
          <AlertDialogCancel onClick={onCancel} disabled={isDeleting}>
            Cancel
          </AlertDialogCancel>
          <AlertDialogAction variant="destructive" onClick={onConfirm} disabled={isDeleting}>
            Delete
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
