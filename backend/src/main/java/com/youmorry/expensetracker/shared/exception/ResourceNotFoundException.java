package com.youmorry.expensetracker.shared.exception;

/**
 * リソースが存在しない場合にスローされる例外（HTTP 404）。
 *
 * <p>他ユーザーのリソースへのアクセス時にも使用し、リソースの存在を秘匿する。
 */
public class ResourceNotFoundException extends AppException {

  public ResourceNotFoundException(String detail) {
    super("about:blank", "Not Found", 404, detail);
  }
}
