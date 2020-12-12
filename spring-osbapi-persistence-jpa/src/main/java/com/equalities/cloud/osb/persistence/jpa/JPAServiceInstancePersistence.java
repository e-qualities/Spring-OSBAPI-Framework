package com.equalities.cloud.osb.persistence.jpa;

import static com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceEntity.jpaType;
import static com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceEntity.osbType;

import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;

import reactor.core.publisher.Mono;

public class JPAServiceInstancePersistence implements ServiceInstancePersistence {

  private ServiceInstanceRepository repository;

  public JPAServiceInstancePersistence(ServiceInstanceRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<ServiceInstanceEntity> insert(ServiceInstanceEntity entity) {
    return Mono.just(osbType(repository.save(jpaType(entity))));
  }
  
  @Override
  public Mono<ServiceInstanceEntity> update(ServiceInstanceEntity entity) {
    return Mono.just(osbType(repository.save(jpaType(entity))));
  }

  @Override
  public Mono<Void> delete(ServiceInstanceEntity entity) {
    return Mono.fromCallable(() -> { repository.delete(jpaType(entity)); return null;} ); 
  }

  @Override
  public Mono<ServiceInstanceEntity> readByServiceInstanceId(String serviceInstanceId) {
    return Mono.justOrEmpty(osbType(repository.findById(serviceInstanceId)));
  }
}
