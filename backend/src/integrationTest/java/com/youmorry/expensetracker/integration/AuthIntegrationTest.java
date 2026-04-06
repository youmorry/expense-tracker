package com.youmorry.expensetracker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youmorry.expensetracker.application.auth.OauthUserInfo;
import com.youmorry.expensetracker.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;

/** 認証フロー（Google OAuth → JWT 発行）の一気通貫テスト。 */
class AuthIntegrationTest extends AbstractIntegrationTest {

  @Test
  void authenticateWithGoogle_withValidToken_returnsJwtAndCreatesUser() throws Exception {
    given(oauthTokenVerifier.verify("valid-google-token"))
        .willReturn(new OauthUserInfo("google-sub-123", "test@example.com", "Test User"));

    mockMvc
        .perform(
            post("/api/v1/auth/google")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {"id_token": "valid-google-token"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token", notNullValue()))
        .andExpect(jsonPath("$.user.email").value("test@example.com"))
        .andExpect(jsonPath("$.user.display_name").value("Test User"))
        .andExpect(jsonPath("$.user.id", notNullValue()));
  }

  @Test
  void authenticateWithGoogle_withExistingUser_returnsJwtWithoutDuplicatingUser() throws Exception {
    given(oauthTokenVerifier.verify("valid-google-token"))
        .willReturn(new OauthUserInfo("google-sub-456", "existing@example.com", "Existing User"));

    // 1回目: ユーザー作成
    mockMvc
        .perform(
            post("/api/v1/auth/google")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {"id_token": "valid-google-token"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.id").isNumber());

    // 2回目: 同じユーザーで再認証 → 新規作成されない
    mockMvc
        .perform(
            post("/api/v1/auth/google")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {"id_token": "valid-google-token"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.email").value("existing@example.com"));

    // users テーブルに同一 google_id のレコードが1件のみであることを確認
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE google_id = ?", Integer.class, "google-sub-456");
    assertThat(count).isEqualTo(1);
  }

  @Test
  void authenticateWithGoogle_withInvalidToken_returns401() throws Exception {
    given(oauthTokenVerifier.verify(anyString()))
        .willThrow(new UnauthorizedException("The Google ID token is invalid."));

    mockMvc
        .perform(
            post("/api/v1/auth/google")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {"id_token": "invalid-token"}
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.type").value("/errors/unauthorized"))
        .andExpect(jsonPath("$.title").value("Authentication required."));
  }
}
