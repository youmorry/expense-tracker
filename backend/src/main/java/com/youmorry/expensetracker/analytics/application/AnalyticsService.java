package com.youmorry.expensetracker.analytics.application;

import com.youmorry.expensetracker.analytics.domain.AnalyticsRepository;
import com.youmorry.expensetracker.analytics.domain.CategoryBreakdown;
import com.youmorry.expensetracker.analytics.domain.NeedWantBreakdown;
import com.youmorry.expensetracker.user.domain.UserId;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

/** 分析集計のユースケースを実装する。 */
@Service
public class AnalyticsService {

  private final AnalyticsRepository analyticsRepository;

  /**
   * コンストラクタ。
   *
   * @param analyticsRepository 分析リポジトリ
   */
  public AnalyticsService(AnalyticsRepository analyticsRepository) {
    this.analyticsRepository = analyticsRepository;
  }

  /**
   * カテゴリ別集計を取得する。
   *
   * @param userId ユーザー ID
   * @param from 集計開始日（null の場合は制限なし）
   * @param to 集計終了日（null の場合は制限なし）
   * @return カテゴリ別集計結果
   */
  public CategoryAnalyticsResult getCategoryBreakdown(
      UserId userId, @Nullable LocalDate from, @Nullable LocalDate to) {
    List<CategoryBreakdown> breakdowns =
        analyticsRepository.findCategoryBreakdown(userId, from, to);
    BigDecimal totalAmount =
        breakdowns.stream().map(CategoryBreakdown::amount).reduce(BigDecimal.ZERO, BigDecimal::add);

    List<CategoryAnalyticsResult.Item> items =
        breakdowns.stream().map(b -> toItem(b, totalAmount)).toList();
    return new CategoryAnalyticsResult(totalAmount, items);
  }

  private CategoryAnalyticsResult.Item toItem(CategoryBreakdown breakdown, BigDecimal totalAmount) {
    BigDecimal percentage = calculatePercentage(breakdown.amount(), totalAmount);
    return new CategoryAnalyticsResult.Item(
        breakdown.categoryId(),
        breakdown.name(),
        breakdown.amount(),
        breakdown.transactionCount(),
        percentage);
  }

  /**
   * {@link NeedWantType}ごとの集計を取得する。
   *
   * @param userId ユーザー ID
   * @param from 集計開始日（null の場合は制限なし）
   * @param to 集計終了日（null の場合は制限なし）
   * @return need/want 別集計結果
   */
  public NeedWantAnalyticsResult getNeedWantBreakdown(
      UserId userId, @Nullable LocalDate from, @Nullable LocalDate to) {
    List<NeedWantBreakdown> breakdowns =
        analyticsRepository.findNeedWantBreakdown(userId, from, to);
    BigDecimal totalAmount =
        breakdowns.stream().map(NeedWantBreakdown::amount).reduce(BigDecimal.ZERO, BigDecimal::add);

    List<NeedWantAnalyticsResult.Item> items =
        breakdowns.stream().map(b -> toNeedWantItem(b, totalAmount)).toList();
    return new NeedWantAnalyticsResult(totalAmount, items);
  }

  private NeedWantAnalyticsResult.Item toNeedWantItem(
      NeedWantBreakdown breakdown, BigDecimal totalAmount) {
    BigDecimal percentage = calculatePercentage(breakdown.amount(), totalAmount);
    return new NeedWantAnalyticsResult.Item(
        breakdown.type(), breakdown.amount(), breakdown.transactionCount(), percentage);
  }

  private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal totalAmount) {
    if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
      return new BigDecimal("0.0");
    }
    return amount.multiply(BigDecimal.valueOf(100)).divide(totalAmount, 1, RoundingMode.HALF_UP);
  }
}
