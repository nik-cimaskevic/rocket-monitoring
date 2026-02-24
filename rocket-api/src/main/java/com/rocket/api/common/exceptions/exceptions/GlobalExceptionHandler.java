package com.rocket.api.common.exceptions.exceptions;

import com.rocket.api.common.exceptions.exceptions.status.*;
import com.rocket.api.common.uuid.UuidProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final UuidProvider uuidGenerator;

  /**
   * Protective layer to make sure that we do not expose the internal database structure to the outside world.
   */
  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<?> handleException(DataAccessException exception) {
    log.error("An unexpected persistence error occurred. ({})", exception.getMessage(), exception);
    return new ResponseEntity<>(
        ErrorResponse.internalServerError(uuidGenerator.nextUuidV7()),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<?> handleException(ApplicationException exception) {
    switch (exception) {
      case ResourceOrEntityNotFoundException e -> {
        return new ResponseEntity<>(
            ErrorResponse.responseWithoutCustomDataModel(new NotFound(), e.errorMessage, e.errorCode, uuidGenerator.nextUuidV7()),
            HttpStatus.NOT_FOUND);
      }
      case BusinessRuleViolationException e -> {
        return new ResponseEntity<>(
            new ErrorResponse(new Conflict(), e.errorMessage, e.errorCode, uuidGenerator.nextUuidV7(), e.getCustomErrorData()),
            HttpStatus.CONFLICT);
      }
      case ValidationException e -> {
        return new ResponseEntity<>(
            ErrorResponse.responseWithoutCustomDataModel(new BadRequest(), e.errorMessage, e.errorCode, uuidGenerator.nextUuidV7()),
            HttpStatus.BAD_REQUEST);
      }
      case ForbiddenException e -> {
        return new ResponseEntity<>(
            ErrorResponse.responseWithoutCustomDataModel(new Forbidden(), e.errorMessage, e.errorCode, uuidGenerator.nextUuidV7()),
            HttpStatus.FORBIDDEN);
      }
      case InternalServerException e -> {
        if (exception.getCause() != null) {
          log.error("An unexpected internal server error occurred.", exception.getCause());
        }
        return new ResponseEntity<>(
            ErrorResponse.responseWithoutCustomDataModel(new InternalServerError(), e.errorMessage, e.errorCode, uuidGenerator.nextUuidV7()),
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
      case UnauthorizedException e -> {
        return new ResponseEntity<>(
            ErrorResponse.responseWithoutCustomDataModel(new Unauthorized(), e.errorMessage, e.errorCode, uuidGenerator.nextUuidV7()),
            HttpStatus.UNAUTHORIZED);
      }
    }
  }


  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<?> handleException(NoResourceFoundException exception) {
    return new ResponseEntity<>(
        ErrorResponse.responseWithoutCustomDataModel(
            new NotFound(), exception.getMessage(), "resource.not.found", uuidGenerator.nextUuidV7()),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleException(Exception exception) {
    log.error("An unexpected error occurred.", exception);
    return new ResponseEntity<>(
        ErrorResponse.internalServerError(uuidGenerator.nextUuidV7()),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
