package com.youmorry.expensetracker.presentation.analytics;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CategoryAnalyticsResponseTest {

  @Test
  void constructor_withNullTotalAmount_throwsNullPointerException() {
    assertThatThrownBy(() -> new CategoryAnalyticsResponse(null, List.of()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void constructor_withNullCategories_throwsNullPointerException() {
    assertThatThrownBy(() -> new CategoryAnalyticsResponse("0", null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void categoryItemConstructor_withNullCategoryName_throwsNullPointerException() {
    assertThatThrownBy(
            () -> new CategoryAnalyticsResponse.CategoryItem(1L, null, "1000", BigDecimal.TEN, 1))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void categoryItemConstructor_withNullAmount_throwsNullPointerException() {
    assertThatThrownBy(
            () -> new CategoryAnalyticsResponse.CategoryItem(1L, "Food", null, BigDecimal.TEN, 1))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void categoryItemConstructor_withNullPercentage_throwsNullPointerException() {
    assertThatThrownBy(
            () -> new CategoryAnalyticsResponse.CategoryItem(1L, "Food", "1000", null, 1))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void categoryItemConstructor_withNegativeTransactionCount_returnsIllegalArgumentException() {
    assertThatThrownBy(
            () -> new CategoryAnalyticsResponse.CategoryItem(1L, "Food", "1000", BigDecimal.TEN, -1))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
