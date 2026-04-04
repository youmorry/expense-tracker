package com.youmorry.expensetracker.integration;

import com.youmorry.expensetracker.application.auth.JwtTokenGenerator;
import com.youmorry.expensetracker.application.auth.OauthTokenVerifier;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.testutil.SharedPostgresContainer;
import java.math.BigDecimal;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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

  protected UserId insertUser(String googleId, String email, String displayName) {
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

  /** スケール（小数桁数）を無視して金額を数値比較する Hamcrest マッチャー。 */
  protected static Matcher<String> amountEqualTo(String expected) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(String actual) {
        return new BigDecimal(actual).compareTo(new BigDecimal(expected)) == 0;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("amount numerically equal to ").appendValue(expected);
      }
    };
  }
}
