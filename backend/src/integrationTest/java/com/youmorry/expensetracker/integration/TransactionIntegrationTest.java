package com.youmorry.expensetracker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.youmorry.expensetracker.user.domain.UserId;
import org.junit.jupiter.api.Test;

/** コア CRUD（Transaction）の一気通貫テスト。 */
class TransactionIntegrationTest extends AbstractIntegrationTest {

  @Test
  void create_withValidJwt_returns201AndPersistsToDb() throws Exception {
    UserId userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    String token = generateToken(userId, "user1@example.com");

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
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.date").value("2026-03-15"))
        .andExpect(jsonPath("$.amount", amountEqualTo("1500")))
        .andExpect(jsonPath("$.category_id").value(1))
        .andExpect(jsonPath("$.category_name").value("Food"))
        .andExpect(jsonPath("$.need_want_type").value("NEED"))
        .andExpect(jsonPath("$.title").value("Lunch"))
        .andExpect(jsonPath("$.created_at", notNullValue()));

    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM transactions WHERE user_id = ?", Integer.class, userId.value());
    assertThat(count).isEqualTo(1);
  }

  @Test
  void get_ofAnotherUser_returns404() throws Exception {
    UserId user1 = insertUser("google-sub-1", "user1@example.com", "User 1");
    UserId user2 = insertUser("google-sub-2", "user2@example.com", "User 2");

    // user1 が Transaction を作成
    String createResponse =
        mockMvc
            .perform(
                post("/api/v1/transactions")
                    .header("Authorization", generateToken(user1, "user1@example.com"))
                    .contentType(APPLICATION_JSON)
                    .content(
                        """
                        {"date": "2026-03-15", "amount": "1000"}
                        """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    int transactionId = JsonPath.read(createResponse, "$.id");

    // user2 が user1 の Transaction を取得しようとする → 404（存在秘匿）
    mockMvc
        .perform(
            get("/api/v1/transactions/" + transactionId)
                .header("Authorization", generateToken(user2, "user2@example.com")))
        .andExpect(status().isNotFound());
  }

  @Test
  void update_withValidData_returns200AndUpdatesDb() throws Exception {
    UserId userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    String token = generateToken(userId, "user1@example.com");

    String createResponse =
        mockMvc
            .perform(
                post("/api/v1/transactions")
                    .header("Authorization", token)
                    .contentType(APPLICATION_JSON)
                    .content(
                        """
                        {"date": "2026-03-15", "amount": "1000", "title": "Original"}
                        """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    int transactionId = JsonPath.read(createResponse, "$.id");

    mockMvc
        .perform(
            put("/api/v1/transactions/" + transactionId)
                .header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {"date": "2026-03-20", "amount": "2000", "title": "Updated"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.date").value("2026-03-20"))
        .andExpect(jsonPath("$.amount", amountEqualTo("2000")))
        .andExpect(jsonPath("$.title").value("Updated"));
  }

  @Test
  void delete_withValidId_returns204AndRemovesFromDb() throws Exception {
    UserId userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    String token = generateToken(userId, "user1@example.com");

    String createResponse =
        mockMvc
            .perform(
                post("/api/v1/transactions")
                    .header("Authorization", token)
                    .contentType(APPLICATION_JSON)
                    .content(
                        """
                        {"date": "2026-03-15", "amount": "500"}
                        """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    int transactionId = JsonPath.read(createResponse, "$.id");

    mockMvc
        .perform(delete("/api/v1/transactions/" + transactionId).header("Authorization", token))
        .andExpect(status().isNoContent());

    // DB から削除されていることを確認
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM transactions WHERE id = ?", Integer.class, transactionId);
    assertThat(count).isZero();
  }

  @Test
  void fullCrudLifecycle_createReadUpdateDelete() throws Exception {
    UserId userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    String token = generateToken(userId, "user1@example.com");

    // Create
    String createResponse =
        mockMvc
            .perform(
                post("/api/v1/transactions")
                    .header("Authorization", token)
                    .contentType(APPLICATION_JSON)
                    .content(
                        """
                        {
                          "date": "2026-04-01",
                          "amount": "3000",
                          "category_id": 6,
                          "need_want_type": "WANT",
                          "title": "Movie ticket",
                          "memo": "Weekend treat"
                        }
                        """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    int transactionId = JsonPath.read(createResponse, "$.id");

    // Read
    mockMvc
        .perform(get("/api/v1/transactions/" + transactionId).header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.amount", amountEqualTo("3000")))
        .andExpect(jsonPath("$.category_name").value("Entertainment"))
        .andExpect(jsonPath("$.need_want_type").value("WANT"))
        .andExpect(jsonPath("$.title").value("Movie ticket"))
        .andExpect(jsonPath("$.memo").value("Weekend treat"));

    // Update
    mockMvc
        .perform(
            put("/api/v1/transactions/" + transactionId)
                .header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "date": "2026-04-01",
                      "amount": "3500",
                      "category_id": 6,
                      "need_want_type": "WANT",
                      "title": "Movie ticket + popcorn"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.amount", amountEqualTo("3500")))
        .andExpect(jsonPath("$.title").value("Movie ticket + popcorn"));

    // Delete
    mockMvc
        .perform(delete("/api/v1/transactions/" + transactionId).header("Authorization", token))
        .andExpect(status().isNoContent());

    // Read after delete → 404
    mockMvc
        .perform(get("/api/v1/transactions/" + transactionId).header("Authorization", token))
        .andExpect(status().isNotFound());
  }
}
