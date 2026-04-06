package com.youmorry.expensetracker.integration;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youmorry.expensetracker.application.auth.OauthUserInfo;
import com.youmorry.expensetracker.domain.user.UserId;
import org.junit.jupiter.api.Test;

/** 横断的セキュリティの統合テスト。認証が必要なエンドポイントと公開エンドポイントのアクセス制御を検証する。 */
class SecurityIntegrationTest extends AbstractIntegrationTest {

  @Test
  void authenticatedEndpoint_withoutToken_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void authenticatedEndpoint_withExpiredToken_returns401() throws Exception {
    UserId userId = insertUser("google-sub-1", "user1@example.com", "User 1");
    String expiredToken = generateExpiredToken(userId, "user1@example.com");

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", expiredToken))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void googleAuth_withValidIdToken_returns200() throws Exception {
    given(oauthTokenVerifier.verify("valid-google-token"))
        .willReturn(new OauthUserInfo("google-sub-1", "user1@example.com", "User 1"));

    mockMvc
        .perform(
            post("/api/v1/auth/google")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {"id_token": "valid-google-token"}
                    """))
        .andExpect(status().isOk());
  }
}
