package com.equalities.cloud.osb.persistence.jpa;

import static com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceBindingOperationEntity.jpaType;
import static com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceBindingOperationEntity.osbType;

import java.time.Duration;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;

import reactor.core.publisher.Mono;

public class JPAServiceInstanceBindingOperationPersistence implements ServiceInstanceBindingOperationPersistence {
  
  private ServiceInstanceBindingOperationRepository repository;

  public JPAServiceInstanceBindingOperationPersistence(ServiceInstanceBindingOperationRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<ServiceInstanceBindingOperationEntity> insert(ServiceInstanceBindingOperationEntity operationEntity) {
    return Mono.just(osbType(repository.save(jpaType(operationEntity))));
  }

  @Override
  public Mono<ServiceInstanceBindingOperationEntity> update(ServiceInstanceBindingOperationEntity operationEntity) {
    return Mono.just(osbType(repository.save(jpaType(operationEntity))));
  }

  @Override
  public Mono<Void> delete(ServiceInstanceBindingOperationEntity operationEntity) {
    return Mono.fromCallable(() -> { repository.delete(jpaType(operationEntity)); return null;});
  }

  @Override
  public Mono<ServiceInstanceBindingOperationEntity> readByOperationId(String operationId) {
    return Mono.justOrEmpty(osbType(repository.findById(operationId)));
  }

  @Override
  public Mono<Void> deleteOperationsOlderThan(Duration duration) {
    return Mono.fromCallable(() -> { repository.deleteOperationsOlderThan(duration); return null;});
  }

  @Override
  public Mono<Void> deleteOperationsByStateOlderThan(State state, Duration duration) {
    return Mono.fromCallable(() -> { repository.deleteOperationsByStateOlderThan(state, duration); return null;});
  }
}
