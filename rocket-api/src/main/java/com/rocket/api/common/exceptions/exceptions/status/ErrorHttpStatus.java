package com.rocket.api.common.exceptions.exceptions.status;

import com.fasterxml.jackson.annotation.JsonValue;

public sealed abstract class ErrorHttpStatus permits BadRequest, Conflict, Forbidden, InternalServerError, NotFound, Unauthorized{
  private final Integer code;

  public ErrorHttpStatus(int code) {
    this.code = code;
  }

  @JsonValue
  public int getCode() {
    return code;
  }
}
