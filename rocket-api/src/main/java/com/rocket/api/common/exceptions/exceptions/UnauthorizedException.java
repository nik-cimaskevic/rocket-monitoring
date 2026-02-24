package com.rocket.api.common.exceptions.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Value;

/*
 * An exception for when a user is not authenticated properly, for example if an ipcheck fails.
 */

@EqualsAndHashCode(callSuper = true)
@Value
public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException() {
        super("unauthorized.error", "User is not authorized to access this resource");
    }

}
