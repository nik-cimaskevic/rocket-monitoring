package com.rocket.api.common.exceptions.exceptions;

/**
 * This class will be used in {@link BusinessRuleViolationException} class to define the custom data class which should be passed to the {@link ErrorResponse} object.
 * This is needed in order for global handler to detect the custom error class and send it to the client.
 */
public abstract class CustomErrorDataAbstract {}
