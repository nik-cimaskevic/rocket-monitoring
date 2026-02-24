package com.rocket.api.common.exceptions.exceptions;

import lombok.Getter;

/*
 * Abstract class which will be parent for all custom exceptions in the application. This way we will be able to enforce fields and catch
 * this exception in global handlers and easily map it to the error response.
 */
@Getter
public sealed abstract class ApplicationException extends RuntimeException permits BusinessRuleViolationException, ForbiddenException,
    InternalServerException, ResourceOrEntityNotFoundException, UnauthorizedException, ValidationException {

  protected final String errorCode;
  protected final String errorMessage;

  public ApplicationException(String errorCode, String errorMessage) {
    super(errorMessage);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  /*
   * This constructor is used when we want to wrap another exception and pass it to the global handler. This way we can log the original
   * exception and still return the custom error response.
   */
  public ApplicationException(String errorCode, String errorMessage, Throwable cause) {
    super(errorMessage, cause);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }
}
