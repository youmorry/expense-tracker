package com.youmorry.expensetracker.application;

import com.youmorry.expensetracker.application.port.JwtTokenGenerator;
import com.youmorry.expensetracker.application.port.OauthTokenVerifier;
import com.youmorry.expensetracker.application.port.OauthUserInfo;
import com.youmorry.expensetracker.domain.user.CurrencyCode;
import com.youmorry.expensetracker.domain.user.LocaleCurrencyMapper;
import com.youmorry.expensetracker.domain.user.User;
import com.youmorry.expensetracker.domain.user.UserRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** OAuth 認証 → ユーザー取得/作成 → JWT 発行のユースケースを実装する。 */
@Service
public class AuthService {

  private final OauthTokenVerifier oauthTokenVerifier;
  private final UserRepository userRepository;
  private final JwtTokenGenerator jwtTokenGenerator;

  /**
   * コンストラクタ。
   *
   * @param oauthTokenVerifier OAuth トークン検証器
   * @param userRepository ユーザーリポジトリ
   * @param jwtTokenGenerator JWT トークン生成器
   */
  public AuthService(
      OauthTokenVerifier oauthTokenVerifier,
      UserRepository userRepository,
      JwtTokenGenerator jwtTokenGenerator) {
    this.oauthTokenVerifier = oauthTokenVerifier;
    this.userRepository = userRepository;
    this.jwtTokenGenerator = jwtTokenGenerator;
  }

  /**
   * OAuth ID トークンを検証し、ユーザーを取得または作成して JWT を発行する。
   *
   * <p>注意: {@code @Transactional} のスコープ内に外部 HTTP 呼び出し（{@link OauthTokenVerifier#verify}）が
   * 含まれており、Google の応答遅延時に DB コネクションを長時間保持するリスクがある。 現時点では個人利用で同時リクエスト数が
   * 極めて少ないため許容している。将来マルチユーザー化する場合は {@code TransactionTemplate} で DB 操作のみにトランザクション範囲を絞ること。
   *
   * @param idToken OAuth ID トークン文字列
   * @param locale Accept-Language ヘッダーから解決されたロケール
   * @return 認証結果（アクセストークンとユーザー情報）
   * @throws com.youmorry.expensetracker.shared.exception.UnauthorizedException トークン検証失敗時
   * @see <a href="https://github.com/youmorry/expense-tracker/issues/34">Issue #34</a>
   */
  @Transactional
  public AuthResult authenticate(String idToken, Locale locale) {
    OauthUserInfo userInfo = oauthTokenVerifier.verify(idToken);

    User user =
        userRepository
            .findByGoogleId(userInfo.subject())
            .orElseGet(() -> createNewUser(userInfo, locale));

    String accessToken = jwtTokenGenerator.generateToken(user.id(), user.email());
    return new AuthResult(accessToken, user);
  }

  private User createNewUser(OauthUserInfo userInfo, Locale locale) {
    CurrencyCode currencyCode = LocaleCurrencyMapper.toCurrencyCode(locale);
    User newUser =
        User.createNew(userInfo.subject(), userInfo.email(), userInfo.name(), currencyCode);
    return userRepository.save(newUser);
  }

  /** 認証結果を保持する。 */
  public record AuthResult(String accessToken, User user) {}
}
