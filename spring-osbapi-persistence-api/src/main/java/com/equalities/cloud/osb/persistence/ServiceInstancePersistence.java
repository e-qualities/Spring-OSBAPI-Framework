package com.equalities.cloud.osb.persistence;

import reactor.core.publisher.Mono;

public interface ServiceInstancePersistence {

  Mono<ServiceInstanceEntity> insert(ServiceInstanceEntity entity);
  
  Mono<ServiceInstanceEntity> update(ServiceInstanceEntity entity);

  Mono<Void> delete(ServiceInstanceEntity entity);

  Mono<ServiceInstanceEntity> readByServiceInstanceId(String serviceInstanceId);
}
