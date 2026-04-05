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

  /**
   * フィールドごとのエラー詳細を表すレコード。
   *
   * @param detail エラーの詳細な説明
   * @param field エラーが発生した Java フィールドの論理名（例: "categoryId"）
   */
  public record FieldError(String detail, String field) {}
}
