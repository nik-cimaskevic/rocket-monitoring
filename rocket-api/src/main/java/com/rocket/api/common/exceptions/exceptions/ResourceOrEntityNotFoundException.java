package com.rocket.api.common.exceptions.exceptions;

/*
 * Exception which primarily will be thrown when a resource or entity was not found during request or business logic flow.
 * Class is final since we do not need to extend it, since it is enough to provide the resource or entity name which was not found and it
 * will compile the error message based on that.
 *
 * This exception will be handled in ExceptionGlobalHandler and mapped to the ErrorResponse object which will be returned to the client with 404 error.
 */
public final class ResourceOrEntityNotFoundException extends ApplicationException {

  public ResourceOrEntityNotFoundException(String resourceName) {
    super("resource.or.entity." + normalizeResourceName(resourceName) + ".was.not.found",
        "The resource or entity " + resourceName + " was not found during request or business logic flow.");
  }

  public ResourceOrEntityNotFoundException(String resourceName, String resourceId) {
    super("resource.or.entity." + normalizeResourceName(resourceName) + ".was.not.found",
        "The resource or entity " + resourceName + " with id " + resourceId + " was not found during request or business logic flow.");
  }


  private static String normalizeResourceName(String resourceName) {
    return resourceName.toLowerCase().replace(" ", ".");
  }
}
