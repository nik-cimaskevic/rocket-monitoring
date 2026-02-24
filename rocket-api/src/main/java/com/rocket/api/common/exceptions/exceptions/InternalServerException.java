package com.rocket.api.common.exceptions.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class InternalServerException extends ApplicationException {

  public InternalServerException() {
    super("internal.server.error", "Internal server error");
  }

  public InternalServerException(Throwable cause) {
    super("internal.server.error", "Internal server error", cause);
  }
}
