/** @see docs/03-design/backend/api-design.md */

import { useMutation, type UseMutationResult } from "@tanstack/react-query";

import { apiClient } from "../../../lib/api/client";

export function useDeleteAccount(): UseMutationResult<void, Error, void> {
  return useMutation({
    mutationFn: async (): Promise<void> => {
      await apiClient.del("/api/v1/users/me");
    },
  });
}
