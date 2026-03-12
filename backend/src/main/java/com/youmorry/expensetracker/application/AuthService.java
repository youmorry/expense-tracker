package com.youmorry.expensetracker.application;

import com.youmorry.expensetracker.application.port.JwtTokenGenerator;
import com.youmorry.expensetracker.application.port.OauthTokenVerifier;
import com.youmorry.expensetracker.application.port.OauthUserInfo;
import com.youmorry.expensetracker.domain.model.user.LocaleCurrencyMapper;
import com.youmorry.expensetracker.domain.model.user.User;
import com.youmorry.expensetracker.domain.model.user.UserRepository;
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
   * @param idToken OAuth ID トークン文字列
   * @return 認証結果（アクセストークンとユーザー情報）
   * @throws com.youmorry.expensetracker.shared.exception.UnauthorizedException トークン検証失敗時
   */
  @Transactional
  public AuthResult authenticate(String idToken) {
    OauthUserInfo userInfo = oauthTokenVerifier.verify(idToken);

    User user =
        userRepository.findByGoogleId(userInfo.subject()).orElseGet(() -> createNewUser(userInfo));

    String accessToken = jwtTokenGenerator.generateToken(user.id(), user.email());
    return new AuthResult(accessToken, user);
  }

  private User createNewUser(OauthUserInfo userInfo) {
    String currencyCode = LocaleCurrencyMapper.toCurrencyCode(userInfo.locale());
    User newUser =
        User.createNew(userInfo.subject(), userInfo.email(), userInfo.name(), currencyCode);
    return userRepository.save(newUser);
  }

  /** 認証結果を保持する。 */
  public record AuthResult(String accessToken, User user) {}
}
