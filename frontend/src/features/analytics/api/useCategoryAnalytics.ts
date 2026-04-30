/**
 * カテゴリ別支出分析の取得フック。
 *
 * `Period` (`null` のとき期間指定なし＝全期間) を受け取り、
 * `GET /api/v1/analytics/category` を呼び出す。
 *
 * @see docs/03-design/backend/api-design.md
 */

import { useQuery, type UseQueryResult } from "@tanstack/react-query";

import type { Period } from "../../../components/period";
import { apiClient } from "../../../lib/api/client";
import { CategoryAnalyticsSchema } from "../../../types/api";
import type { CategoryAnalytics } from "../types";

function buildPath(period: Period): string {
  if (period === null) return "/api/v1/analytics/category";
  const search = new URLSearchParams({ from: period.from, to: period.to });
  return `/api/v1/analytics/category?${search.toString()}`;
}

export function useCategoryAnalytics(period: Period): UseQueryResult<CategoryAnalytics> {
  return useQuery({
    queryKey: ["analytics", "category", period],
    queryFn: async (): Promise<CategoryAnalytics> => {
      const data = await apiClient.get(buildPath(period));
      return CategoryAnalyticsSchema.parse(data);
    },
  });
}
