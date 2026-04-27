/** @see docs/03-design/backend/api-design.md */

import { useMutation, useQueryClient, type UseMutationResult } from "@tanstack/react-query";

import { apiClient } from "../../../lib/api/client";
import {
  type CreateTransactionRequest,
  type Transaction,
  TransactionSchema,
} from "../../../types/api";

export function useCreateTransaction(): UseMutationResult<
  Transaction,
  Error,
  CreateTransactionRequest
> {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: CreateTransactionRequest): Promise<Transaction> => {
      const data = await apiClient.post("/api/v1/transactions", input);
      return TransactionSchema.parse(data);
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["transactions"] });
    },
  });
}
