package com.rocket.api.common.exceptions.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Value;

/*
 * An exception for when a user is not allowed to access a resource.
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class ForbiddenException extends ApplicationException {

    public ForbiddenException() {
        super("forbidden.error", "User is not allowed to access this resource");
    }
}
