package com.equalities.cloud.osb.persistence;

public class ServiceInstanceBindingOperationNotFoundException extends RuntimeException {
  private static final long serialVersionUID = -3279384969619079226L;

  public ServiceInstanceBindingOperationNotFoundException() {
  }

  public ServiceInstanceBindingOperationNotFoundException(String message) {
    super(message);
  }

  public ServiceInstanceBindingOperationNotFoundException(Throwable cause) {
    super(cause);
  }

  public ServiceInstanceBindingOperationNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceInstanceBindingOperationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
