package com.youmorry.expensetracker.presentation.auth;

import com.youmorry.expensetracker.application.AuthService;
import com.youmorry.expensetracker.application.AuthService.AuthResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
   * @param httpRequest HTTP リクエスト（Accept-Language ヘッダーからロケールを取得）
   * @return JWT アクセストークンとユーザー情報を含むレスポンス
   */
  @PostMapping("/google")
  public ResponseEntity<AuthResponse> authenticateWithGoogle(
      @Valid @RequestBody AuthRequest request, HttpServletRequest httpRequest) {
    AuthResult result = authService.authenticate(request.idToken(), httpRequest.getLocale());
    return ResponseEntity.ok(AuthResponse.from(result));
  }
}
