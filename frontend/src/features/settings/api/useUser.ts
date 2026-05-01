/**
 * 認証済みユーザーの情報を取得するフック。
 *
 * @see GET /api/v1/users/me
 */

import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../../../lib/api/client";
import { UserSchema } from "../../../types/api";

const STALE_TIME_MS = 5 * 60 * 1000;

export function useUser() {
  return useQuery({
    queryKey: ["users", "me"],
    queryFn: async () => {
      const data = await apiClient.get("/api/v1/users/me");
      return UserSchema.parse(data);
    },
    staleTime: STALE_TIME_MS,
  });
}
