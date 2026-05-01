/**
 * 認証済みユーザー情報の取得フック。
 *
 * プロフィール変更は稀だが反映遅延を許容しすぎないよう staleTime は 5 分に設定する。
 *
 * @see docs/03-design/backend/api-design.md
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
