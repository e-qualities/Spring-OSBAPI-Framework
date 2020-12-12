package com.equalities.cloud.osb.persistence;

import java.time.Duration;

import reactor.core.publisher.Mono;

/**
 * Persistence interface to be implemented by concrete persistence layers.
 * Actual persistence layer implementations will be provided via separate Spring Boot starters
 * and configured via auto-configuration. 
 */
public interface ServiceInstanceBindingOperationPersistence {

  /**
   * Stores an operation instance to persistent storage.
   * @param operation the operation instance to store to the persistence layer.
   * @return a {code Mono} providing the stored operation.
   */
  Mono<ServiceInstanceBindingOperationEntity> insert(ServiceInstanceBindingOperationEntity operation);

  /**
   * Updates an existing operation instance in persistent storage.
   * @param operation the operation instance to update the persistence layer with.
   * @return a {@code Mono} providing the updated operation.
   */
  Mono<ServiceInstanceBindingOperationEntity> update(ServiceInstanceBindingOperationEntity operation);

  /**
   * Deletes a stored operation instance from persistent storage.
   * @param operationId the ID of the operation to be removed.
   * @return a {@code Mono} indicating when deletion has finished.
   */
  Mono<Void> delete(ServiceInstanceBindingOperationEntity operation);
  
  /**
   * Deletes stored operation instances by their state from persistent storage.
   * @param duration the age of the operations to delete.
   * @return a {@code Mono} indicating when deletion has finished.
   */
  Mono<Void> deleteOperationsOlderThan(Duration duration);
  
  /**
   * Deletes stored operation instances by their state from persistent storage.
   * @param state the state of the operations to delete.
   * @param duration the duration indicating the age of the operation.
   * @return a {@code Mono} indicating when deletion has finished.
   */
  Mono<Void> deleteOperationsByStateOlderThan(ServiceOperationStatus.State state, Duration duration);

  /**
   * Returns a stored operation by its operation ID. The value of the returned {@code Mono}
   * should be null, if the operation for the given ID could not be found. 
   * @param operationId the ID of the operation to return from storage.
   * @return a {@code Mono} holding the value of the returned operation or null, if none could be found.
   */
  Mono<ServiceInstanceBindingOperationEntity> readByOperationId(String operationId);
}