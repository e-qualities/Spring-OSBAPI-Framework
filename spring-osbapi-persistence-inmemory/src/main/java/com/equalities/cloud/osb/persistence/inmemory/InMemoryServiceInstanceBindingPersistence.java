package com.equalities.cloud.osb.persistence.inmemory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;

import reactor.core.publisher.Mono;

/**
 * Simple in-memory implementation of {@link ServiceInstanceBindingPersistence}.
 * You can set a delay used during persistence to simulate the asynchronous nature of this API.
 * Default delay is 0 seconds. You can change it by using the constructor taking a delay.
 * <p>
 * Intended mainly for testing and debugging purposes. 
 * Don't use this in production unless you know what you are doing.
 */
public class InMemoryServiceInstanceBindingPersistence implements ServiceInstanceBindingPersistence {

  private final Map<String, ServiceInstanceBindingEntity> serviceBindings;
  private Duration delay = Duration.ofSeconds(0);
  
  /**
   * Creates a new instance with a default persistence delay of 0 seconds.
   */
  public InMemoryServiceInstanceBindingPersistence() {
    this.serviceBindings = new HashMap<>();
  }
  
  /**
   * Creates a new instance with the given delay.
   * @param delay the delay to use during persistence. Used for simulation of long-running persistence processes.
   */
  public InMemoryServiceInstanceBindingPersistence(Duration delay) {
    this.serviceBindings = new HashMap<>();
    this.delay = delay;
  }
  
  //test constructor
  protected InMemoryServiceInstanceBindingPersistence(Map<String, ServiceInstanceBindingEntity> memory) {
    this.serviceBindings = memory;
  }
  
  @Override
  public Mono<ServiceInstanceBindingEntity> insert(ServiceInstanceBindingEntity entity) {
    return Mono.delay(delay).flatMap( time -> {
      serviceBindings.put(entity.getServiceInstanceBindingId(), entity);
      return Mono.just(entity);
    });
  }

  @Override
  public Mono<ServiceInstanceBindingEntity> update(ServiceInstanceBindingEntity entity) {
    return insert(entity);
  }

  @Override
  public Mono<Void> delete(ServiceInstanceBindingEntity instance) {
    return Mono.delay(delay).flatMap( time -> {
      serviceBindings.remove(instance.getServiceInstanceBindingId());
      return Mono.empty();
    });
  }

  @Override
  public Mono<ServiceInstanceBindingEntity> readByServiceInstanceBindingId(String id) {
    return Mono.delay(delay).flatMap( time -> {
      ServiceInstanceBindingEntity instance = serviceBindings.get(id);
      
      if(instance == null) {
        return Mono.empty();
      }
      
      return Mono.just(instance);
    });
  }
}
