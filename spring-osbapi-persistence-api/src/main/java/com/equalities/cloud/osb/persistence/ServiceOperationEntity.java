package com.equalities.cloud.osb.persistence;

/**
 * Common superclass of {@link ServiceInstanceOperationEntity} and {@link ServiceInstanceBindingOperationEntity}.
 */
public abstract class ServiceOperationEntity {
  
  public enum Type {
    CREATE, UPDATE, DELETE
  }
}
