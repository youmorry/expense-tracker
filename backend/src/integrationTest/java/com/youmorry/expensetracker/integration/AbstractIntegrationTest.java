package com.youmorry.expensetracker.integration;

import com.youmorry.expensetracker.application.auth.JwtTokenGenerator;
import com.youmorry.expensetracker.application.auth.OauthTokenVerifier;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.testutil.SharedPostgresContainer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 統合テストの基底クラス。
 *
 * <p>全 Bean を実際にワイヤリングし、MockMvc 経由でフルリクエストライフサイクルを検証する。唯一のモックは {@link OauthTokenVerifier}（外部 Google
 * API 通信の遮断）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
abstract class AbstractIntegrationTest {

  @ServiceConnection static final PostgreSQLContainer POSTGRES = SharedPostgresContainer.INSTANCE;

  @Autowired protected MockMvc mockMvc;

  @Autowired protected JwtTokenGenerator jwtTokenGenerator;

  @Autowired protected JdbcTemplate jdbcTemplate;

  @MockitoBean protected OauthTokenVerifier oauthTokenVerifier;

  @BeforeEach
  void cleanDatabase() {
    jdbcTemplate.execute("TRUNCATE TABLE transactions, users CASCADE");
  }

  protected String generateToken(UserId userId, String email) {
    return "Bearer " + jwtTokenGenerator.generateToken(userId, email);
  }
}
