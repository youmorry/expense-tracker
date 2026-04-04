package com.youmorry.expensetracker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** 分析エンドポイント（カテゴリ別集計・need/want 比率）の一気通貫テスト。 */
class AnalyticsIntegrationTest extends AbstractIntegrationTest {

  @Test
  void categoryBreakdown_withTransactions_returnsCorrectAggregation() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    // Food x2, Transport x1
    createTransaction(token, "2026-03-15", "1000", 1, "NEED", "Lunch");
    createTransaction(token, "2026-03-20", "2000", 1, "NEED", "Dinner");
    createTransaction(token, "2026-03-15", "500", 2, "NEED", "Bus");

    mockMvc
        .perform(get("/api/v1/analytics/category").header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_amount", amountEqualTo("3500")))
        // categories は amount DESC でソート → Food(3000) が先
        .andExpect(jsonPath("$.categories[0].category_name").value("Food"))
        .andExpect(jsonPath("$.categories[0].amount", amountEqualTo("3000")))
        .andExpect(jsonPath("$.categories[0].transaction_count").value(2))
        .andExpect(jsonPath("$.categories[1].category_name").value("Transport"))
        .andExpect(jsonPath("$.categories[1].amount", amountEqualTo("500")))
        .andExpect(jsonPath("$.categories[1].transaction_count").value(1));
  }

  @Test
  void categoryBreakdown_withDateRange_returnsFilteredAggregation() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    createTransaction(token, "2026-02-28", "1000", 1, "NEED", "Feb item");
    createTransaction(token, "2026-03-15", "2000", 1, "NEED", "Mar item");
    createTransaction(token, "2026-04-01", "3000", 1, "NEED", "Apr item");

    mockMvc
        .perform(
            get("/api/v1/analytics/category")
                .header("Authorization", token)
                .param("from", "2026-03-01")
                .param("to", "2026-03-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_amount", amountEqualTo("2000")));
  }

  @Test
  void categoryBreakdown_withNoTransactions_returnsAllCategoriesWithZeroAmount() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    mockMvc
        .perform(get("/api/v1/analytics/category").header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_amount", amountEqualTo("0")))
        // 全11カテゴリが返る（amount=0 でも含まれる）
        .andExpect(jsonPath("$.categories", hasSize(11)))
        .andExpect(jsonPath("$.categories[0].amount", amountEqualTo("0")));
  }

  @Test
  void needWantBreakdown_withTransactions_returnsCorrectBreakdown() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    createTransaction(token, "2026-03-15", "3000", 1, "NEED", "Groceries");
    createTransaction(token, "2026-03-15", "1000", 6, "WANT", "Movie");
    createTransaction(token, "2026-03-15", "500", 1, "WANT", "Snack");

    var json =
        mockMvc
            .perform(get("/api/v1/analytics/need-want").header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_amount", amountEqualTo("4500")))
            .andExpect(jsonPath("$.breakdown", hasSize(3)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    var breakdown = parseBreakdown(json);
    assertNeedWantItem(breakdown, "NEED", "3000", 1);
    assertNeedWantItem(breakdown, "WANT", "1500", 2);
    assertNeedWantItem(breakdown, "UNSET", "0", 0);
  }

  @Test
  void needWantBreakdown_withNoTransactions_returnsAllTypesWithZeroAmount() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    var json =
        mockMvc
            .perform(get("/api/v1/analytics/need-want").header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_amount", amountEqualTo("0")))
            .andExpect(jsonPath("$.breakdown", hasSize(3)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    var breakdown = parseBreakdown(json);
    assertNeedWantItem(breakdown, "NEED", "0", 0);
    assertNeedWantItem(breakdown, "WANT", "0", 0);
    assertNeedWantItem(breakdown, "UNSET", "0", 0);
  }

  @Test
  void analytics_returnsOnlyOwnData() throws Exception {
    var user1 = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token1 = generateToken(user1, "user1@example.com");
    var user2 = insertUser("google-sub-2", "user2@example.com", "User 2");
    var token2 = generateToken(user2, "user2@example.com");

    createTransaction(token1, "2026-03-15", "1000", 1, "NEED", "User1 tx");
    createTransaction(token2, "2026-03-15", "9000", 1, "NEED", "User2 tx");

    // user1 の集計には user2 のデータが含まれない
    mockMvc
        .perform(get("/api/v1/analytics/category").header("Authorization", token1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_amount", amountEqualTo("1000")));

    mockMvc
        .perform(get("/api/v1/analytics/need-want").header("Authorization", token1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_amount", amountEqualTo("1000")));
  }

  /** Breakdown 配列を type をキーとする Map に変換する。 */
  private Map<String, Map<String, Object>> parseBreakdown(String json) {
    List<Map<String, Object>> items = JsonPath.read(json, "$.breakdown");
    return items.stream()
        .collect(Collectors.toMap(item -> (String) item.get("type"), item -> item));
  }

  private void assertNeedWantItem(
      Map<String, Map<String, Object>> breakdown,
      String type,
      String expectedAmount,
      int expectedCount) {
    assertThat(breakdown).containsKey(type);
    var item = breakdown.get(type);
    assertThat(new BigDecimal(item.get("amount").toString()))
        .isEqualByComparingTo(new BigDecimal(expectedAmount));
    assertThat(((Number) item.get("transaction_count")).intValue()).isEqualTo(expectedCount);
  }
}
