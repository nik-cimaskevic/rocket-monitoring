package com.rocket.api.common.exceptions.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rocket.api.common.exceptions.exceptions.status.BadRequest;
import com.rocket.api.common.exceptions.exceptions.status.ErrorHttpStatus;
import com.rocket.api.common.exceptions.exceptions.status.InternalServerError;
import lombok.NonNull;

import java.util.UUID;

/*
 * This is generic error which will be returned to the client in case of any exception.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @NonNull ErrorHttpStatus status,
    @NonNull String message,
    @NonNull String errorCode,
    @NonNull UUID traceId,
    CustomErrorDataAbstract data
) {

  /*
   * Error without custom data model. Will be used in 99% of cases. Custom data model is only used when we need to add additional data model to the
   * error response, so frontend can parse it and execute complex error handling logic based on the error.
   */
  public static ErrorResponse responseWithoutCustomDataModel(
      @NonNull ErrorHttpStatus errorHttpStatus,
      @NonNull String message,
      @NonNull String errorCode,
      @NonNull UUID traceId) {
    return new ErrorResponse(errorHttpStatus, message, errorCode, traceId, null);
  }

  /*
   * Generic response with predefined error code and http status code
   */
  public static ErrorResponse internalServerError(@NonNull UUID traceId) {
    return new ErrorResponse(new InternalServerError(), "Internal service error occurred", "internal.server.error", traceId, null);
  }

  /*
   * Generic response with predefined error code and http status code which will be used to map jakarta validation errors in global exception handler.
   * We can use generic error code here, because normally this error won't be parsed by Frontend, but rather it will be thrown while front end developers
   * are testing endpoints manually.
   */
  public static ErrorResponse validationError(@NonNull String message , @NonNull UUID traceId) {
    return new ErrorResponse(new BadRequest(), message, "validation.error", traceId, null);
  }
}
