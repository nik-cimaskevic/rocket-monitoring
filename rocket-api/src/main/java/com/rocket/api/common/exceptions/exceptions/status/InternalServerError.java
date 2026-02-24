package com.rocket.api.common.exceptions.exceptions.status;

public final class InternalServerError extends ErrorHttpStatus {
  public InternalServerError() {
    super(500);
  }
}