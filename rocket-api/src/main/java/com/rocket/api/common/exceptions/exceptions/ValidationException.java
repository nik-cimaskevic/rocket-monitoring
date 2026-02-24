package com.rocket.api.common.exceptions.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Value;

/*
 * Exception which primarily need to be thrown when validation of the input data failed. Or when there is complex validation object on the edge of
 * the system like user case command. If you want throw it from domain layer it better to do with business rule violation exception.
 *
 * The class is final which means not extendable since it cover all validation cases.
 *
 * This exception will be handled in ExceptionGlobalHandler and mapped to the ErrorResponse object which will be returned to the client with 400 error.
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class ValidationException extends ApplicationException {
  public ValidationException(String errorCode, String message) {
    super("validation.error." + errorCode.toLowerCase().replace(" ", "."), message);
  }
}
