package com.equalities.cloud.osb.persistence.inmemory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;

import reactor.core.publisher.Mono;

/**
 * Simple in-memory implementation of {@link ServiceInstancePersistence}.
 * You can set a delay used during persistence to simulate the asynchronous nature of this API.
 * Default delay is 0 seconds. You can change it by using the constructor taking a delay.
 * <p>
 * Intended mainly for testing and debugging purposes. 
 * Don't use this in production unless you know what you are doing.
 */
public class InMemoryServiceInstancePersistence implements ServiceInstancePersistence {

  private final Map<String, ServiceInstanceEntity> serviceInstances;
  private Duration delay = Duration.ofSeconds(0);
  
  /**
   * Creates a new instance with a default persistence delay of 0 seconds.
   */
  public InMemoryServiceInstancePersistence() {
    this.serviceInstances = new HashMap<>(); 
  }
  
  /**
   * Creates a new instance with the given delay.
   * @param delay the delay to use during persistence. Used for simulation of long-running persistence processes.
   */
  public InMemoryServiceInstancePersistence(Duration delay) {
    this.serviceInstances = new HashMap<>();
    this.delay = delay;
  }
  
  //test constructor
  public InMemoryServiceInstancePersistence(Map<String, ServiceInstanceEntity> memory) {
    this.serviceInstances = memory;
  }
  
  @Override
  public Mono<ServiceInstanceEntity> insert(ServiceInstanceEntity entity) {
    return Mono.delay(delay).flatMap( time -> {
      serviceInstances.put(entity.getServiceInstanceId(), entity);
      return Mono.just(entity);
    });
  }

  @Override
  public Mono<ServiceInstanceEntity> update(ServiceInstanceEntity entity) {
    return insert(entity);
  }

  @Override
  public Mono<Void> delete(ServiceInstanceEntity instance) {
    return Mono.delay(delay).flatMap( time -> {
      serviceInstances.remove(instance.getServiceInstanceId());
      return Mono.empty();
    });
  }

  @Override
  public Mono<ServiceInstanceEntity> readByServiceInstanceId(String id) {
    return Mono.delay(delay).flatMap( time -> {
      ServiceInstanceEntity instance = serviceInstances.get(id);
      
      if(instance == null) {
        return Mono.empty();
      }
      
      return Mono.just(instance);
    });
  }
}
