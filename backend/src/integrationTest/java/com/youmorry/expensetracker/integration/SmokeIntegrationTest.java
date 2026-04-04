package com.youmorry.expensetracker.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/** Spring コンテキストの起動とセキュリティ設定の基本動作を検証するスモークテスト。 */
class SmokeIntegrationTest extends AbstractIntegrationTest {

  @Test
  void contextLoads() {
    // Spring コンテキストが正常に起動することを確認（テストメソッド内の処理は不要）
  }

  @Test
  void authenticatedEndpoint_withoutToken_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void authEndpoint_withoutToken_returns200OrMethodSpecific() throws Exception {
    // /api/v1/auth/** は permitAll なので 401 にならないことを確認
    // POST のみ受け付けるエンドポイントの場合、GET は 405 になる可能性がある
    mockMvc
        .perform(get("/api/v1/auth/google"))
        .andExpect(
            result -> {
              int statusCode = result.getResponse().getStatus();
              assert statusCode != 401 : "Auth endpoint should not return 401, got: " + statusCode;
            });
  }
}
