package com.youmorry.expensetracker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/** ユーザーエンドポイントの統合テスト。 */
class UserIntegrationTest extends AbstractIntegrationTest {

  @Test
  void getMe_withValidJwt_returns200WithUserInfo() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.value()))
        .andExpect(jsonPath("$.email").value("user1@example.com"))
        .andExpect(jsonPath("$.display_name").value("User 1"))
        .andExpect(jsonPath("$.created_at", notNullValue()));
  }

  @Test
  void deleteAccount_withValidJwt_returns204AndCascadeDeletesTransactions() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    // Transaction を作成
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "date": "2026-03-15",
                      "amount": "1500",
                      "category_id": 1,
                      "need_want_type": "NEED",
                      "title": "Lunch"
                    }
                    """))
        .andExpect(status().isCreated());

    // アカウント削除
    mockMvc
        .perform(delete("/api/v1/users/me").header("Authorization", token))
        .andExpect(status().isNoContent());

    // ユーザーが削除されたことを確認
    var userCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId.value());
    assertThat(userCount).isZero();

    // Transaction もカスケード削除されたことを確認
    var txCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM transactions WHERE user_id = ?", Integer.class, userId.value());
    assertThat(txCount).isZero();
  }
}
