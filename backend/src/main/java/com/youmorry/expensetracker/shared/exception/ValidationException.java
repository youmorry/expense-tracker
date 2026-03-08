package com.youmorry.expensetracker.shared.exception;

import java.util.List;

/**
 * ビジネスルールに基づくバリデーションエラー時にスローされる例外（HTTP 422）。
 *
 * <p>フィールドごとのエラー詳細を {@link FieldError} のリストとして保持する。
 */
public class ValidationException extends AppException {

  private final List<FieldError> errors;

  /** 指定されたエラー詳細でインスタンスを生成する。 */
  public ValidationException(String detail, List<FieldError> errors) {
    super("/errors/validation-error", "Your request is not valid.", 422, detail);
    this.errors = errors;
  }

  public List<FieldError> getErrors() {
    return errors;
  }

  /** フィールド単位のバリデーションエラー。 */
  public record FieldError(String detail, String pointer) {}
}
