package com.youmorry.expensetracker.presentation.analytics;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youmorry.expensetracker.application.analytics.AnalyticsService;
import com.youmorry.expensetracker.application.analytics.CategoryAnalyticsResult;
import com.youmorry.expensetracker.application.analytics.NeedWantAnalyticsResult;
import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.infrastructure.security.SecurityConfig;
import com.youmorry.expensetracker.shared.infrastructure.web.WebMvcConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
@Import({SecurityConfig.class, WebMvcConfig.class})
class AnalyticsControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AnalyticsService analyticsService;

  @Test
  void getCategoryBreakdown_withoutJwt_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/analytics/category")).andExpect(status().isUnauthorized());
  }

  @Test
  void getCategoryBreakdown_withoutDateParams_returns200WithResult() throws Exception {
    CategoryAnalyticsResult result = buildResult();
    when(analyticsService.getCategoryBreakdown(eq(new UserId(1L)), isNull(), isNull()))
        .thenReturn(result);

    mockMvc
        .perform(get("/api/v1/analytics/category").with(jwt().jwt(j -> j.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_amount").value("130000"))
        .andExpect(jsonPath("$.categories").isArray())
        .andExpect(jsonPath("$.categories.length()").value(1))
        .andExpect(jsonPath("$.categories[0].category_id").value(1))
        .andExpect(jsonPath("$.categories[0].category_name").value("Food"))
        .andExpect(jsonPath("$.categories[0].amount").value("45000"))
        .andExpect(jsonPath("$.categories[0].percentage").value(34.6))
        .andExpect(jsonPath("$.categories[0].transaction_count").value(28));
  }

  @Test
  void getCategoryBreakdown_withDateParams_returns200WithResult() throws Exception {
    CategoryAnalyticsResult result = buildResult();
    when(analyticsService.getCategoryBreakdown(
            eq(new UserId(1L)), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 3, 31))))
        .thenReturn(result);

    mockMvc
        .perform(
            get("/api/v1/analytics/category")
                .with(jwt().jwt(j -> j.subject("1")))
                .param("from", "2026-01-01")
                .param("to", "2026-03-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_amount").value("130000"))
        .andExpect(jsonPath("$.categories.length()").value(1));
  }

  @Test
  void getNeedWantBreakdown_withoutJwt_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/analytics/need-want")).andExpect(status().isUnauthorized());
  }

  @Test
  void getNeedWantBreakdown_withoutDateParams_returns200WithResult() throws Exception {
    NeedWantAnalyticsResult result = buildNeedWantResult();
    when(analyticsService.getNeedWantBreakdown(eq(new UserId(1L)), isNull(), isNull()))
        .thenReturn(result);

    mockMvc
        .perform(get("/api/v1/analytics/need-want").with(jwt().jwt(j -> j.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_amount").value("130000"))
        .andExpect(jsonPath("$.breakdown").isArray())
        .andExpect(jsonPath("$.breakdown.length()").value(3))
        .andExpect(jsonPath("$.breakdown[0].type").value("NEED"))
        .andExpect(jsonPath("$.breakdown[0].amount").value("80000"))
        .andExpect(jsonPath("$.breakdown[0].percentage").value(61.5))
        .andExpect(jsonPath("$.breakdown[0].transaction_count").value(45))
        .andExpect(jsonPath("$.breakdown[1].type").value("WANT"))
        .andExpect(jsonPath("$.breakdown[1].amount").value("35000"))
        .andExpect(jsonPath("$.breakdown[1].percentage").value(26.9))
        .andExpect(jsonPath("$.breakdown[1].transaction_count").value(12))
        .andExpect(jsonPath("$.breakdown[2].type").value("UNSET"))
        .andExpect(jsonPath("$.breakdown[2].amount").value("15000"))
        .andExpect(jsonPath("$.breakdown[2].percentage").value(11.5))
        .andExpect(jsonPath("$.breakdown[2].transaction_count").value(3));
  }

  @Test
  void getNeedWantBreakdown_withDateParams_returns200WithResult() throws Exception {
    NeedWantAnalyticsResult result = buildNeedWantResult();
    when(analyticsService.getNeedWantBreakdown(
            eq(new UserId(1L)), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 3, 31))))
        .thenReturn(result);

    mockMvc
        .perform(
            get("/api/v1/analytics/need-want")
                .with(jwt().jwt(j -> j.subject("1")))
                .param("from", "2026-01-01")
                .param("to", "2026-03-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_amount").value("130000"))
        .andExpect(jsonPath("$.breakdown.length()").value(3));
  }

  private CategoryAnalyticsResult buildResult() {
    var item =
        new CategoryAnalyticsResult.Item(
            new CategoryId(1L), "Food", new BigDecimal("45000"), 28, new BigDecimal("34.6"));
    return new CategoryAnalyticsResult(new BigDecimal("130000"), List.of(item));
  }

  private NeedWantAnalyticsResult buildNeedWantResult() {
    var items =
        List.of(
            new NeedWantAnalyticsResult.Item(
                NeedWantType.NEED, new BigDecimal("80000"), 45, new BigDecimal("61.5")),
            new NeedWantAnalyticsResult.Item(
                NeedWantType.WANT, new BigDecimal("35000"), 12, new BigDecimal("26.9")),
            new NeedWantAnalyticsResult.Item(
                NeedWantType.UNSET, new BigDecimal("15000"), 3, new BigDecimal("11.5")));
    return new NeedWantAnalyticsResult(new BigDecimal("130000"), items);
  }
}
