/**
 * カテゴリ一覧取得フック。
 *
 * カテゴリは全ユーザー共通かつ変更頻度が極めて低いため、
 * staleTime を 30 分に設定してリクエストを抑制する。
 *
 * @see docs/03-design/backend/api-design.md
 */

import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../../../lib/api/client";
import { CategorySchema, listResponseSchema } from "../../../types/api";

const CategoriesResponseSchema = listResponseSchema(CategorySchema);

const STALE_TIME_MS = 30 * 60 * 1000;

export function useCategories() {
  return useQuery({
    queryKey: ["categories"],
    queryFn: async () => {
      const data = await apiClient.get("/api/v1/categories");
      return CategoriesResponseSchema.parse(data);
    },
    staleTime: STALE_TIME_MS,
  });
}
