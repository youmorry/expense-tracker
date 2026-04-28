/** @see docs/03-design/backend/api-design.md */

import { useMutation, useQueryClient, type UseMutationResult } from "@tanstack/react-query";

import { apiClient } from "../../../lib/api/client";
import {
  type Transaction,
  type UpdateTransactionRequest,
  TransactionSchema,
} from "../../../types/api";

export function useUpdateTransaction(
  id: number,
): UseMutationResult<Transaction, Error, UpdateTransactionRequest> {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: UpdateTransactionRequest): Promise<Transaction> => {
      const data = await apiClient.put(`/api/v1/transactions/${id.toString()}`, input);
      return TransactionSchema.parse(data);
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["transactions"] });
    },
  });
}
