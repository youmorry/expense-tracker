package com.youmorry.expensetracker.integration;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.youmorry.expensetracker.domain.user.UserId;
import org.junit.jupiter.api.Test;

/** コア CRUD（Transaction）の一気通貫テスト。 */
class TransactionIntegrationTest extends AbstractIntegrationTest {

  @Test
  void create_withValidJwt_returns201AndPersistsToDb() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

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
        .andExpect(jsonPath("$.amount").value("1500"))
        .andExpect(jsonPath("$.category_id").value(1))
        .andExpect(jsonPath("$.category_name").value("Food"))
        .andExpect(jsonPath("$.need_want_type").value("NEED"))
        .andExpect(jsonPath("$.title").value("Lunch"))
        .andExpect(jsonPath("$.created_at", notNullValue()));

    var count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM transactions WHERE user_id = ?", Integer.class, userId.value());
    assert count == 1 : "Expected 1 transaction but found " + count;
  }

  @Test
  void get_ofAnotherUser_returns404() throws Exception {
    var user1 = insertUser("google-sub-1", "user1@example.com", "User 1");
    var user2 = insertUser("google-sub-2", "user2@example.com", "User 2");

    // user1 が Transaction を作成
    var createResponse =
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
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    var createResponse =
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
        .andExpect(jsonPath("$.amount").value("2000"))
        .andExpect(jsonPath("$.title").value("Updated"));
  }

  @Test
  void delete_withValidId_returns204AndRemovesFromDb() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    var createResponse =
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
    var count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM transactions WHERE id = ?", Integer.class, transactionId);
    assert count == 0 : "Expected 0 transactions but found " + count;
  }

  @Test
  void fullCrudLifecycle_createReadUpdateDelete() throws Exception {
    var userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    var token = generateToken(userId, "user1@example.com");

    // Create
    var createResponse =
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
        .andExpect(jsonPath("$.amount").value("3000.0000"))
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
        .andExpect(jsonPath("$.amount").value("3500"))
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

  private UserId insertUser(String googleId, String email, String displayName) {
    jdbcTemplate.update(
        "INSERT INTO users (google_id, email, display_name) VALUES (?, ?, ?)",
        googleId,
        email,
        displayName);
    var id =
        jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE google_id = ?", Long.class, googleId);
    return new UserId(id);
  }
}
