package com.equalities.cloud.osb.persistence;

public class ServiceInstanceNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1810484564006762336L;

  public ServiceInstanceNotFoundException() {
  }

  public ServiceInstanceNotFoundException(String message) {
    super(message);
  }

  public ServiceInstanceNotFoundException(Throwable cause) {
    super(cause);
  }

  public ServiceInstanceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceInstanceNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
