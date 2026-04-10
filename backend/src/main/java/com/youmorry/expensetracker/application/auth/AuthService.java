package com.youmorry.expensetracker.application.auth;

import com.youmorry.expensetracker.user.domain.User;
import com.youmorry.expensetracker.user.domain.UserRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** OAuth 認証 → ユーザー取得/作成 → JWT 発行のユースケースを実装する。 */
@Service
public class AuthService {

  private static final String DEFAULT_DISPLAY_NAME = "USER";

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
   * @return 認証結果（アクセストークンとユーザー情報）
   * @throws com.youmorry.expensetracker.shared.exception.UnauthorizedException トークン検証失敗時
   * @see <a href="https://github.com/youmorry/expense-tracker/issues/34">Issue #34</a>
   */
  @Transactional
  public AuthResult authenticate(String idToken) {
    OauthUserInfo userInfo = oauthTokenVerifier.verify(idToken);

    User user =
        userRepository.findByGoogleId(userInfo.subject()).orElseGet(() -> createNewUser(userInfo));

    String accessToken =
        jwtTokenGenerator.generateToken(Objects.requireNonNull(user.id()), user.email());
    return new AuthResult(accessToken, user);
  }

  private User createNewUser(OauthUserInfo userInfo) {
    String displayName =
        (userInfo.name() == null || userInfo.name().isBlank())
            ? DEFAULT_DISPLAY_NAME
            : userInfo.name();
    User newUser = User.createNew(userInfo.subject(), userInfo.email(), displayName);
    return userRepository.save(newUser);
  }

  /** 認証結果を保持する。 */
  public record AuthResult(String accessToken, User user) {}
}
