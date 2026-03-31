package com.youmorry.expensetracker.application.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.youmorry.expensetracker.domain.analytics.AnalyticsRepository;
import com.youmorry.expensetracker.domain.analytics.CategoryBreakdown;
import com.youmorry.expensetracker.domain.analytics.NeedWantBreakdown;
import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import com.youmorry.expensetracker.domain.user.UserId;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

  @Mock private AnalyticsRepository analyticsRepository;
  @InjectMocks private AnalyticsService analyticsService;

  @Test
  void getCategoryBreakdown_withCategories_returnsCorrectPercentages() {
    var userId = new UserId(1L);
    var from = LocalDate.of(2026, 1, 1);
    var to = LocalDate.of(2026, 3, 31);
    var breakdowns =
        List.of(
            new CategoryBreakdown(new CategoryId(1L), "Food", new BigDecimal("1000"), 5),
            new CategoryBreakdown(new CategoryId(2L), "Transport", new BigDecimal("500"), 3));
    when(analyticsRepository.findCategoryBreakdown(userId, from, to)).thenReturn(breakdowns);

    var result = analyticsService.getCategoryBreakdown(userId, from, to);

    assertEquals(new BigDecimal("1500"), result.totalAmount());
    assertEquals(2, result.breakdown().size());

    var food = result.breakdown().get(0);
    assertEquals(new CategoryId(1L), food.categoryId());
    assertEquals("Food", food.name());
    assertEquals(new BigDecimal("1000"), food.amount());
    assertEquals(5L, food.transactionCount());
    assertEquals(new BigDecimal("66.7"), food.percentage());

    var transport = result.breakdown().get(1);
    assertEquals(new CategoryId(2L), transport.categoryId());
    assertEquals(new BigDecimal("33.3"), transport.percentage());
  }

  @Test
  void getCategoryBreakdown_withZeroTotalAmount_returnsZeroPercentage() {
    var userId = new UserId(1L);
    var breakdowns =
        List.of(
            new CategoryBreakdown(new CategoryId(1L), "Food", BigDecimal.ZERO, 0),
            new CategoryBreakdown(new CategoryId(2L), "Transport", BigDecimal.ZERO, 0));
    when(analyticsRepository.findCategoryBreakdown(userId, null, null)).thenReturn(breakdowns);

    var result = analyticsService.getCategoryBreakdown(userId, null, null);

    assertEquals(BigDecimal.ZERO, result.totalAmount());
    result.breakdown().forEach(item -> assertEquals(new BigDecimal("0.0"), item.percentage()));
  }

  @Test
  void getNeedWantBreakdown_withBreakdowns_returnsCorrectPercentages() {
    var userId = new UserId(1L);
    var from = LocalDate.of(2026, 1, 1);
    var to = LocalDate.of(2026, 3, 31);
    var breakdowns =
        List.of(
            new NeedWantBreakdown(NeedWantType.NEED, new BigDecimal("8000"), 10),
            new NeedWantBreakdown(NeedWantType.WANT, new BigDecimal("2000"), 5),
            new NeedWantBreakdown(NeedWantType.UNSET, BigDecimal.ZERO, 0));
    when(analyticsRepository.findNeedWantBreakdown(userId, from, to)).thenReturn(breakdowns);

    var result = analyticsService.getNeedWantBreakdown(userId, from, to);

    assertEquals(new BigDecimal("10000"), result.totalAmount());
    assertEquals(3, result.breakdown().size());

    var need = result.breakdown().get(0);
    assertEquals(NeedWantType.NEED, need.type());
    assertEquals(new BigDecimal("8000"), need.amount());
    assertEquals(10L, need.transactionCount());
    assertEquals(new BigDecimal("80.0"), need.percentage());

    var want = result.breakdown().get(1);
    assertEquals(NeedWantType.WANT, want.type());
    assertEquals(new BigDecimal("20.0"), want.percentage());

    var unset = result.breakdown().get(2);
    assertEquals(NeedWantType.UNSET, unset.type());
    assertEquals(new BigDecimal("0.0"), unset.percentage());
  }

  @Test
  void getNeedWantBreakdown_withZeroTotalAmount_returnsZeroPercentage() {
    var userId = new UserId(1L);
    var breakdowns =
        List.of(
            new NeedWantBreakdown(NeedWantType.NEED, BigDecimal.ZERO, 0),
            new NeedWantBreakdown(NeedWantType.WANT, BigDecimal.ZERO, 0),
            new NeedWantBreakdown(NeedWantType.UNSET, BigDecimal.ZERO, 0));
    when(analyticsRepository.findNeedWantBreakdown(userId, null, null)).thenReturn(breakdowns);

    var result = analyticsService.getNeedWantBreakdown(userId, null, null);

    assertEquals(BigDecimal.ZERO, result.totalAmount());
    result.breakdown().forEach(item -> assertEquals(new BigDecimal("0.0"), item.percentage()));
  }
}
