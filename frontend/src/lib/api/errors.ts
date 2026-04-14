import type { ApiError } from "../../types/api";

export class ApiException extends Error {
  constructor(
    public readonly status: number,
    public readonly apiError: ApiError,
  ) {
    super(apiError.title);
    this.name = "ApiException";
  }
}

export class NetworkException extends Error {
  constructor(cause: unknown) {
    super("Network error");
    this.name = "NetworkException";
    this.cause = cause;
  }
}
