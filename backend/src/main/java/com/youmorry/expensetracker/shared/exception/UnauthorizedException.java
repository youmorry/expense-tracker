package com.youmorry.expensetracker.shared.exception;

/**
 * 認証に失敗した場合にスローされる例外（HTTP 401）。
 *
 * <p>JWT が未送信・期限切れ・署名不正の場合に使用する。
 */
public class UnauthorizedException extends AppException {

  /** 指定されたエラー詳細でインスタンスを生成する。 */
  public UnauthorizedException(String detail) {
    super("/errors/unauthorized", "Authentication required.", 401, detail);
  }
}
