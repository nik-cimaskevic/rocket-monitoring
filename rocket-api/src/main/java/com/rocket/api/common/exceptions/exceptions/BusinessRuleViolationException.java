package com.rocket.api.common.exceptions.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Value;

/*
 * Exception which primarily used during domain logic flow when business rule was violated. So it should be used either in business layer or application layer.
 * Class is final and not extendable, all errors should be classified in BusinessRuleError class child classes.
 * This exception will be handled in ExceptionGlobalHandler and mapped to the ErrorResponse object which will be returned to the client with 409 error.
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class BusinessRuleViolationException extends ApplicationException {
  CustomErrorDataAbstract customErrorData;

  private BusinessRuleViolationException(BusinessRuleError businessRuleError) {
    super(businessRuleError.errorCode(), businessRuleError.message());
    this.customErrorData = null;
  }

  private BusinessRuleViolationException(BusinessRuleError businessRuleError, CustomErrorDataAbstract customErrorData) {
    super(businessRuleError.errorCode(), businessRuleError.message());
    this.customErrorData = customErrorData;
  }


  public static BusinessRuleViolationException withError(BusinessRuleError businessRuleError) {
    return new BusinessRuleViolationException(businessRuleError);
  }

  public static BusinessRuleViolationException withError(BusinessRuleError businessRuleError, CustomErrorDataAbstract customErrorData) {
    return new BusinessRuleViolationException(businessRuleError, customErrorData);
  }
}
