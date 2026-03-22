package com.youmorry.expensetracker.presentation.auth;

import com.youmorry.expensetracker.application.AuthService;
import com.youmorry.expensetracker.application.AuthService.AuthResult;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 認証エンドポイント。Google ID トークンの検証と JWT 発行を行う。 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  /**
   * コンストラクタ。
   *
   * @param authService 認証サービス
   */
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * Google ID トークンを検証し、JWT アクセストークンを発行する。
   *
   * @param request Google 認証リクエスト
   * @param acceptLanguage Accept-Language ヘッダー値（通貨コード推定に使用）。null 可
   * @return JWT アクセストークンとユーザー情報を含むレスポンス
   */
  @PostMapping("/google")
  public ResponseEntity<AuthResponse> authenticateWithGoogle(
      @Valid @RequestBody AuthRequest request,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) @Nullable
          String acceptLanguage) {
    String locale = parsePreferredLocale(acceptLanguage);
    AuthResult result = authService.authenticate(request.idToken(), locale);
    return ResponseEntity.ok(AuthResponse.from(result));
  }

  /**
   * Accept-Language ヘッダーから最優先の locale 文字列を抽出する。
   *
   * @param acceptLanguage Accept-Language ヘッダー値（例: "ja-JP,ja;q=0.9,en-US;q=0.8"）。null 可
   * @return 最優先の BCP 47 locale 文字列（例: "ja-JP"）。ヘッダーが null の場合は null
   */
  private static @Nullable String parsePreferredLocale(@Nullable String acceptLanguage) {
    if (acceptLanguage == null || acceptLanguage.isBlank()) {
      return null;
    }
    List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(acceptLanguage);
    return ranges.getFirst().getRange();
  }
}
