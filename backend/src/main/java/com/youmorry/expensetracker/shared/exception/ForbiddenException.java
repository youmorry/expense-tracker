package com.youmorry.expensetracker.shared.exception;

/**
 * 認可に失敗した場合にスローされる例外（HTTP 403）。
 *
 * <p>通常は {@link ResourceNotFoundException}（404）で代替し、リソースの存在を秘匿する。
 */
public class ForbiddenException extends AppException {

  public ForbiddenException(String detail) {
    super("/errors/forbidden", "Forbidden.", 403, detail);
  }
}
