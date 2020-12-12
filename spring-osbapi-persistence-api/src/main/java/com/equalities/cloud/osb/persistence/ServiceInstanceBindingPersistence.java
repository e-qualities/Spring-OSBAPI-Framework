package com.equalities.cloud.osb.persistence;

import reactor.core.publisher.Mono;

public interface ServiceInstanceBindingPersistence {

  Mono<ServiceInstanceBindingEntity> insert(ServiceInstanceBindingEntity entity);
  
  Mono<ServiceInstanceBindingEntity> update(ServiceInstanceBindingEntity entity);

  Mono<Void> delete(ServiceInstanceBindingEntity entity);

  Mono<ServiceInstanceBindingEntity> readByServiceInstanceBindingId(String serviceInstanceBindingId);
}
