package com.rocket.api.common.exceptions.exceptions;


/*
 * This is the data type which will be used for all errors definitions in each module in the system.
 * This class is mainly needed in order to have one common place per module where all errors will be defined.
 */
public record BusinessRuleError(
    String errorCode,
    String message
) {

}
