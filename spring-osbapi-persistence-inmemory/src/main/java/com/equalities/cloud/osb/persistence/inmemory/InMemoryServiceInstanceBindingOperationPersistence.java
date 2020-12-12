package com.equalities.cloud.osb.persistence.inmemory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;

import reactor.core.publisher.Mono;
/**
 * Simple in-memory implementation of {@link ServiceInstanceOperationPersistence}.
 * You can set a delay used during persistence to simulate the asynchronous nature of this API.
 * Default delay is 0 seconds. You can change it by using the constructor taking a delay.
 * <p>
 * Intended mainly for testing and debugging purposes. 
 * Don't use this in production unless you know what you are doing.
 */
public class InMemoryServiceInstanceBindingOperationPersistence implements ServiceInstanceBindingOperationPersistence {
  
  private final Map<String, ServiceInstanceBindingOperationEntity> serviceBindingOperations;
  private Duration delay = Duration.ofSeconds(0);
  
  /**
   * Creates a new instance with a default persistence delay of 0 seconds.
   */
  public InMemoryServiceInstanceBindingOperationPersistence() {
    this.serviceBindingOperations = new HashMap<>();
  }
  
  /**
   * Creates a new instance with the given delay.
   * @param delay the delay to use during persistence. Used for simulation of long-running persistence processes.
   */
  public InMemoryServiceInstanceBindingOperationPersistence(Duration delay) {
    this.serviceBindingOperations = new HashMap<>();
    this.delay = delay;
  }
  
  //test constructor
  protected InMemoryServiceInstanceBindingOperationPersistence(Map<String, ServiceInstanceBindingOperationEntity> memory) {
    this.serviceBindingOperations = memory;
  }
  
  @Override
  public Mono<ServiceInstanceBindingOperationEntity> insert(ServiceInstanceBindingOperationEntity operation) {
    return Mono.delay(delay).flatMap( time -> {
      serviceBindingOperations.put(operation.getId(), operation);
      return Mono.just(operation);
    });
  }

  @Override
  public Mono<ServiceInstanceBindingOperationEntity> update(ServiceInstanceBindingOperationEntity operation) {
    return Mono.delay(delay).flatMap( time -> {
      serviceBindingOperations.put(operation.getId(), operation);
      return Mono.just(operation);
    });
  }

  @Override
  public Mono<Void> delete(ServiceInstanceBindingOperationEntity operation) {
    return Mono.delay(delay).flatMap( time -> {
      serviceBindingOperations.remove(operation.getId());
      return Mono.empty();
    });
  }

  @Override
  public Mono<ServiceInstanceBindingOperationEntity> readByOperationId(String operationId) {
    return Mono.delay(delay).flatMap( time -> {
      ServiceInstanceBindingOperationEntity operation = serviceBindingOperations.get(operationId);
      
      if(operation == null) {
        return Mono.empty();
      }
      
      return Mono.just(operation);
    });
  }

  @Override
  public Mono<Void> deleteOperationsOlderThan(Duration duration) {
    return Mono.delay(delay).flatMap( time -> {
      Iterator<Entry<String, ServiceInstanceBindingOperationEntity>> iterator = serviceBindingOperations.entrySet().iterator();
      while(iterator.hasNext()) {
        Entry<String, ServiceInstanceBindingOperationEntity> entry = iterator.next();
        ServiceInstanceBindingOperationEntity entity = entry.getValue();
        if(entity.getCreatedAt().isBefore(Instant.now().minus(duration))) {
          iterator.remove();
        }
      }
      return Mono.empty();
    });
  }

  @Override
  public Mono<Void> deleteOperationsByStateOlderThan(ServiceOperationStatus.State state, Duration duration) {
    return Mono.delay(delay).flatMap( time -> {
      Iterator<Entry<String, ServiceInstanceBindingOperationEntity>> iterator = serviceBindingOperations.entrySet().iterator();
      while(iterator.hasNext()) {
        Entry<String, ServiceInstanceBindingOperationEntity> entry = iterator.next();
        ServiceInstanceBindingOperationEntity entity = entry.getValue();
        if(entity.getStatus().getState() == state) {
          if(entity.getCreatedAt().isBefore(Instant.now().minus(duration))) {
            iterator.remove();
          }
        }
      }
      return Mono.empty();
    });
  }
}
