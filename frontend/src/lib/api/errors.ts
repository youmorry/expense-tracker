import type { ApiError } from "../../types/api";

export class ApiException extends Error {
  readonly status: number;
  readonly apiError: ApiError;

  constructor(status: number, apiError: ApiError) {
    super(apiError.title);
    this.name = "ApiException";
    this.status = status;
    this.apiError = apiError;
  }
}

export class NetworkException extends Error {
  constructor(cause: unknown) {
    super("Network error");
    this.name = "NetworkException";
    this.cause = cause;
  }
}
