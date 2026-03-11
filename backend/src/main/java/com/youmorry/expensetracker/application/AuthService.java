package com.youmorry.expensetracker.application;

import com.youmorry.expensetracker.domain.model.user.LocaleCurrencyMapper;
import com.youmorry.expensetracker.domain.model.user.User;
import com.youmorry.expensetracker.domain.model.user.UserRepository;
import com.youmorry.expensetracker.infrastructure.security.GoogleIdTokenPayload;
import com.youmorry.expensetracker.infrastructure.security.GoogleTokenVerifier;
import com.youmorry.expensetracker.infrastructure.security.JwtProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Google 認証 → ユーザー取得/作成 → JWT 発行のユースケースを実装する。 */
@Service
public class AuthService {

  private final GoogleTokenVerifier googleTokenVerifier;
  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;

  /** コンストラクタ。 */
  public AuthService(
      GoogleTokenVerifier googleTokenVerifier,
      UserRepository userRepository,
      JwtProvider jwtProvider) {
    this.googleTokenVerifier = googleTokenVerifier;
    this.userRepository = userRepository;
    this.jwtProvider = jwtProvider;
  }

  /** Google ID トークンを検証し、ユーザーを取得または作成して JWT を発行する。 */
  @Transactional
  public AuthResult authenticate(String idToken) {
    GoogleIdTokenPayload payload = googleTokenVerifier.verify(idToken);

    User user =
        userRepository.findByGoogleId(payload.sub()).orElseGet(() -> createNewUser(payload));

    String accessToken = jwtProvider.generateToken(user);
    return new AuthResult(accessToken, user);
  }

  private User createNewUser(GoogleIdTokenPayload payload) {
    String currencyCode = LocaleCurrencyMapper.toCurrencyCode(payload.locale());
    var newUser =
        new User(null, payload.sub(), payload.email(), payload.name(), currencyCode, null);
    return userRepository.save(newUser);
  }

  /** 認証結果を保持する。 */
  public record AuthResult(String accessToken, User user) {}
}
