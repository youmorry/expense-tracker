package com.youmorry.expensetracker.presentation.auth;

import com.youmorry.expensetracker.application.AuthService;
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

  /** コンストラクタ。 */
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /** Google ID トークンを検証し、JWT アクセストークンを発行する。 */
  @PostMapping("/google")
  public ResponseEntity<AuthResponse> authenticateWithGoogle(
      @Valid @RequestBody AuthRequest request) {
    var result = authService.authenticate(request.idToken());
    return ResponseEntity.ok(AuthResponse.from(result));
  }
}
