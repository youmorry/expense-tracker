package com.youmorry.expensetracker.shared.exception;

/**
 * アプリケーション固有の例外の基底クラス。
 *
 * <p>RFC 9457 Problem Details の {@code type}, {@code title}, {@code status} を保持し、 {@link
 * com.youmorry.expensetracker.presentation.GlobalExceptionHandler} で {@code
 * application/problem+json} レスポンスに変換される。
 */
public abstract class AppException extends RuntimeException {

  private final String type;
  private final String title;
  private final int status;

  protected AppException(String type, String title, int status, String detail) {
    super(detail);
    this.type = type;
    this.title = title;
    this.status = status;
  }

  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public int getStatus() {
    return status;
  }

  public String getDetail() {
    return getMessage();
  }
}
