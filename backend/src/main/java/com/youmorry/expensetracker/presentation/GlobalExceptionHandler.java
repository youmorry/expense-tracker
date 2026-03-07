package com.youmorry.expensetracker.presentation;

import com.youmorry.expensetracker.shared.exception.AppException;
import com.youmorry.expensetracker.shared.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.google.common.base.CaseFormat;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * アプリケーション全体の例外を捕捉し、RFC 9457 Problem Details 形式のレスポンスに変換するグローバル例外ハンドラ。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final MediaType PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON;

  /** アプリケーション固有の例外を Problem Details に変換する。 */
  @ExceptionHandler(AppException.class)
  public ResponseEntity<ProblemDetail> handleAppException(AppException ex,
      HttpServletRequest request) {
    int status = ex.getStatus();
    logByStatus(status, ex, request);

    ProblemDetail problem = ProblemDetail.forStatus(status);
    problem.setType(URI.create(ex.getType()));
    problem.setTitle(ex.getTitle());
    problem.setDetail(ex.getDetail());
    problem.setInstance(URI.create(request.getRequestURI()));

    if (ex instanceof ValidationException validationEx) {
      List<Map<String, String>> errors = validationEx.getErrors().stream()
          .map(e -> Map.of("detail", e.detail(), "pointer", e.pointer())).toList();
      problem.setProperty("errors", errors);
    }

    return ResponseEntity.status(status).contentType(PROBLEM_JSON).body(problem);
  }

  /** Bean Validation（{@code @Valid}）によるバリデーションエラーを処理する。 */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    log.warn("Validation error: method={}, URI={}", request.getMethod(), request.getRequestURI());

    ProblemDetail problem = ProblemDetail.forStatus(422);
    problem.setType(URI.create("/errors/validation-error"));
    problem.setTitle("Your request is not valid.");
    problem.setDetail("One or more fields have validation errors.");
    problem.setInstance(URI.create(request.getRequestURI()));

    List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> Map.of("detail",
            fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid value", "pointer",
            "#/" + toSnakeCase(fe.getField())))
        .toList();
    problem.setProperty("errors", errors);

    return ResponseEntity.status(422).contentType(PROBLEM_JSON).body(problem);
  }

  /** JSON パースエラーを処理する。 */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleMessageNotReadable(HttpMessageNotReadableException ex,
      HttpServletRequest request) {
    log.warn("JSON parse error: method={}, URI={}", request.getMethod(), request.getRequestURI());

    ProblemDetail problem = ProblemDetail.forStatus(400);
    problem.setType(URI.create("about:blank"));
    problem.setTitle("Bad Request");
    problem.setDetail("Failed to parse request body.");
    problem.setInstance(URI.create(request.getRequestURI()));

    return ResponseEntity.status(400).contentType(PROBLEM_JSON).body(problem);
  }

  /** クエリパラメータの型変換エラーを処理する。 */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
      HttpServletRequest request) {
    log.warn("Type mismatch: method={}, URI={}, param={}", request.getMethod(),
        request.getRequestURI(), ex.getName());

    ProblemDetail problem = ProblemDetail.forStatus(400);
    problem.setType(URI.create("about:blank"));
    problem.setTitle("Bad Request");
    problem.setDetail("Invalid value for parameter '" + ex.getName() + "'.");
    problem.setInstance(URI.create(request.getRequestURI()));

    return ResponseEntity.status(400).contentType(PROBLEM_JSON).body(problem);
  }

  /** 予期しない例外を処理する。クライアントには固定メッセージを返し、詳細はログにのみ記録する。 */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
    log.error("Unexpected error: method={}, URI={}", request.getMethod(), request.getRequestURI(),
        ex);

    ProblemDetail problem = ProblemDetail.forStatus(500);
    problem.setType(URI.create("about:blank"));
    problem.setTitle("Internal Server Error");
    problem.setDetail("An unexpected error occurred.");
    problem.setInstance(URI.create(request.getRequestURI()));

    return ResponseEntity.status(500).contentType(PROBLEM_JSON).body(problem);
  }

  private void logByStatus(int status, AppException ex, HttpServletRequest request) {
    if (status == 404) {
      log.debug("Resource not found: method={}, URI={}, detail={}", request.getMethod(),
          request.getRequestURI(), ex.getDetail());
    } else if (status >= 500) {
      log.error("Server error: method={}, URI={}, detail={}", request.getMethod(),
          request.getRequestURI(), ex.getDetail(), ex);
    } else {
      log.warn("Client error: method={}, URI={}, status={}, detail={}", request.getMethod(),
          request.getRequestURI(), status, ex.getDetail());
    }
  }

  private String toSnakeCase(String camelCase) {
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camelCase);
  }
}
