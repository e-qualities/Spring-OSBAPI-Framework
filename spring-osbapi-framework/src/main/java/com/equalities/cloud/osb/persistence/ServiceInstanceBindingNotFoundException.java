package com.equalities.cloud.osb.persistence;

public class ServiceInstanceBindingNotFoundException extends RuntimeException {
  private static final long serialVersionUID = -3279384969619079226L;

  public ServiceInstanceBindingNotFoundException() {
  }

  public ServiceInstanceBindingNotFoundException(String message) {
    super(message);
  }

  public ServiceInstanceBindingNotFoundException(Throwable cause) {
    super(cause);
  }

  public ServiceInstanceBindingNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceInstanceBindingNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
