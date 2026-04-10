package com.youmorry.expensetracker.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youmorry.expensetracker.auth.application.JwtTokenGenerator;
import com.youmorry.expensetracker.auth.application.OauthTokenVerifier;
import com.youmorry.expensetracker.testutil.SharedPostgresContainer;
import com.youmorry.expensetracker.user.domain.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
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

  @Value("${app.auth.jwt-secret}")
  private String jwtSecret;

  @Value("${app.auth.jwt-issuer}")
  private String jwtIssuer;

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
    Long id =
        jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE google_id = ?", Long.class, googleId);
    return new UserId(id);
  }

  /** 有効期限切れの JWT トークンを生成する。セキュリティテスト用。 */
  protected String generateExpiredToken(UserId userId, String email) {
    SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(jwtSecret), "HmacSHA256");
    NimbusJwtEncoder encoder = NimbusJwtEncoder.withSecretKey(key).build();
    Instant now = Instant.now();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(jwtIssuer)
            .subject(String.valueOf(userId.value()))
            .claim("email", email)
            .issuedAt(now.minus(2, ChronoUnit.HOURS))
            .expiresAt(now.minus(1, ChronoUnit.HOURS))
            .build();
    return "Bearer " + encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  /** テスト用の取引を作成するヘルパー。 */
  protected void createTransaction(
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

  /** memo なしの取引作成ヘルパー。 */
  protected void createTransaction(
      String token, String date, String amount, int categoryId, String needWantType, String title)
      throws Exception {
    createTransaction(token, date, amount, categoryId, needWantType, title, null);
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
