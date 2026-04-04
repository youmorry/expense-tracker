package com.youmorry.expensetracker.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/** 支出検索フィルタの一気通貫テスト。 */
class TransactionSearchIntegrationTest extends AbstractIntegrationTest {

  @Test
  void search_withNoParams_returnsAllUserTransactions() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    createTransaction(token, "2026-03-15", "1000", 1, "NEED", "Lunch", null);
    createTransaction(token, "2026-03-20", "2000", 2, "WANT", "Taxi", null);
    createTransaction(token, "2026-03-10", "500", 1, "NEED", "Snack", null);

    // date DESC, created_at DESC の順で返る
    mockMvc
        .perform(get("/api/v1/transactions").header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(3)))
        .andExpect(jsonPath("$.items[0].date").value("2026-03-20"))
        .andExpect(jsonPath("$.items[1].date").value("2026-03-15"))
        .andExpect(jsonPath("$.items[2].date").value("2026-03-10"));
  }

  @Test
  void search_withDateRange_returnsFilteredTransactions() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    createTransaction(token, "2026-02-28", "500", 1, "NEED", "Feb item", null);
    createTransaction(token, "2026-03-01", "1000", 1, "NEED", "Mar start", null);
    createTransaction(token, "2026-03-31", "2000", 1, "NEED", "Mar end", null);
    createTransaction(token, "2026-04-01", "3000", 1, "NEED", "Apr item", null);

    mockMvc
        .perform(
            get("/api/v1/transactions")
                .header("Authorization", token)
                .param("from", "2026-03-01")
                .param("to", "2026-03-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andExpect(jsonPath("$.items[0].title").value("Mar end"))
        .andExpect(jsonPath("$.items[1].title").value("Mar start"));
  }

  @Test
  void search_withCategoryFilter_returnsFilteredTransactions() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    createTransaction(token, "2026-03-15", "1000", 1, "NEED", "Food item", null);
    createTransaction(token, "2026-03-15", "2000", 2, "NEED", "Transport item", null);
    createTransaction(token, "2026-03-15", "3000", 3, "NEED", "Housing item", null);

    mockMvc
        .perform(
            get("/api/v1/transactions").header("Authorization", token).param("category_id", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].category_name").value("Food"));
  }

  @Test
  void search_withMultipleCategoryFilter_returnsFilteredTransactions() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    createTransaction(token, "2026-03-15", "1000", 1, "NEED", "Food item", null);
    createTransaction(token, "2026-03-15", "2000", 2, "NEED", "Transport item", null);
    createTransaction(token, "2026-03-15", "3000", 3, "NEED", "Housing item", null);

    mockMvc
        .perform(
            get("/api/v1/transactions")
                .header("Authorization", token)
                .param("category_id", "1")
                .param("category_id", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(2)));
  }

  @Test
  void search_withNeedWantTypeFilter_returnsFilteredTransactions() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    createTransaction(token, "2026-03-15", "1000", 1, "NEED", "Need item", null);
    createTransaction(token, "2026-03-15", "2000", 1, "WANT", "Want item", null);

    mockMvc
        .perform(
            get("/api/v1/transactions")
                .header("Authorization", token)
                .param("need_want_type", "WANT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].title").value("Want item"));
  }

  @Test
  void search_withKeyword_returnsMatchingTransactions() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    createTransaction(token, "2026-03-15", "1000", 1, "NEED", "Coffee at cafe", null);
    createTransaction(token, "2026-03-15", "2000", 1, "NEED", "Lunch", "at the cafe nearby");
    createTransaction(token, "2026-03-15", "3000", 1, "NEED", "Dinner", null);

    // keyword は title と memo の ILIKE 部分一致
    mockMvc
        .perform(
            get("/api/v1/transactions").header("Authorization", token).param("keyword", "cafe"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(2)));
  }

  @Test
  void search_withCombinedFilters_returnsIntersection() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    // category=1 (Food), NEED, March
    createTransaction(token, "2026-03-15", "1000", 1, "NEED", "Lunch Mar", null);
    // category=1 (Food), WANT, March
    createTransaction(token, "2026-03-20", "2000", 1, "WANT", "Snack Mar", null);
    // category=2 (Transport), NEED, March
    createTransaction(token, "2026-03-15", "3000", 2, "NEED", "Bus Mar", null);
    // category=1 (Food), NEED, April
    createTransaction(token, "2026-04-01", "4000", 1, "NEED", "Lunch Apr", null);

    // category_id=1 AND need_want_type=NEED AND from=2026-03-01 AND to=2026-03-31
    mockMvc
        .perform(
            get("/api/v1/transactions")
                .header("Authorization", token)
                .param("category_id", "1")
                .param("need_want_type", "NEED")
                .param("from", "2026-03-01")
                .param("to", "2026-03-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].title").value("Lunch Mar"));
  }

  @Test
  void search_returnsOnlyOwnTransactions() throws Exception {
    var user1 = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token1 = generateToken(user1, "user1@example.com");
    var user2 = insertUser("google-sub-2", "user2@example.com", "User 2");
    var token2 = generateToken(user2, "user2@example.com");

    createTransaction(token1, "2026-03-15", "1000", 1, "NEED", "User1 tx", null);
    createTransaction(token2, "2026-03-15", "2000", 1, "NEED", "User2 tx", null);

    mockMvc
        .perform(get("/api/v1/transactions").header("Authorization", token1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].title").value("User1 tx"));
  }

  private void createTransaction(
      String token,
      String date,
      String amount,
      int categoryId,
      String needWantType,
      String title,
      String memo)
      throws Exception {
    var memoField = memo != null ? ", \"memo\": \"" + memo + "\"" : "";
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "date": "%s",
                      "amount": "%s",
                      "category_id": %d,
                      "need_want_type": "%s",
                      "title": "%s"%s
                    }
                    """
                        .formatted(date, amount, categoryId, needWantType, title, memoField)))
        .andExpect(status().isCreated());
  }
}
