package com.equalities.cloud.osb.persistence;

public class ServiceInstanceOperationNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1810484564006762336L;

  public ServiceInstanceOperationNotFoundException() {
  }

  public ServiceInstanceOperationNotFoundException(String message) {
    super(message);
  }

  public ServiceInstanceOperationNotFoundException(Throwable cause) {
    super(cause);
  }

  public ServiceInstanceOperationNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceInstanceOperationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
