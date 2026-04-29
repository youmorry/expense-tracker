/** @see docs/03-design/backend/api-design.md */

import { useMutation, useQueryClient, type UseMutationResult } from "@tanstack/react-query";

import { apiClient } from "../../../lib/api/client";

export function useDeleteTransaction(): UseMutationResult<void, Error, number> {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number): Promise<void> => {
      await apiClient.del(`/api/v1/transactions/${id.toString()}`);
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["transactions"] });
    },
  });
}
