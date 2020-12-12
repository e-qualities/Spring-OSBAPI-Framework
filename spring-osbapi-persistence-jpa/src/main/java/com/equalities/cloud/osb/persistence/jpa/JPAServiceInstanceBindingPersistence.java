package com.equalities.cloud.osb.persistence.jpa;

import static com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceBindingEntity.jpaType;
import static com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceBindingEntity.osbType;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;

import reactor.core.publisher.Mono;

public class JPAServiceInstanceBindingPersistence implements ServiceInstanceBindingPersistence {

  private ServiceInstanceBindingRepository repository;

  public JPAServiceInstanceBindingPersistence(ServiceInstanceBindingRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<ServiceInstanceBindingEntity> insert(ServiceInstanceBindingEntity entity) {
    return Mono.just(osbType(repository.save(jpaType(entity))));
  }
  
  @Override
  public Mono<ServiceInstanceBindingEntity> update(ServiceInstanceBindingEntity entity) {
    return Mono.just(osbType(repository.save(jpaType(entity))));
  }

  @Override
  public Mono<Void> delete(ServiceInstanceBindingEntity entity) {
    return Mono.fromCallable(() -> {repository.delete(jpaType(entity)); return null;});
  }

  @Override
  public Mono<ServiceInstanceBindingEntity> readByServiceInstanceBindingId(String serviceInstanceId) {
    return Mono.justOrEmpty(osbType(repository.findById(serviceInstanceId)));
  }
}
