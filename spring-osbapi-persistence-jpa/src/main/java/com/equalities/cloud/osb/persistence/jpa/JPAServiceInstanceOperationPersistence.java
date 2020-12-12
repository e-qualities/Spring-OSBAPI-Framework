package com.equalities.cloud.osb.persistence.jpa;

import static com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceOperationEntity.jpaType;
import static com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceInstanceOperationEntity.osbType;

import java.time.Duration;

import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;

import reactor.core.publisher.Mono;

public class JPAServiceInstanceOperationPersistence implements ServiceInstanceOperationPersistence {

  private ServiceInstanceOperationRepository repository;

  public JPAServiceInstanceOperationPersistence(ServiceInstanceOperationRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<ServiceInstanceOperationEntity> insert(ServiceInstanceOperationEntity operationEntity) {
    return Mono.just(osbType(repository.save(jpaType(operationEntity))));
  }

  @Override
  public Mono<ServiceInstanceOperationEntity> update(ServiceInstanceOperationEntity operationEntity) {
    return Mono.just(osbType(repository.save(jpaType(operationEntity))));
  }

  @Override
  public Mono<Void> delete(ServiceInstanceOperationEntity operationEntity) {
    return Mono.fromCallable(() -> { repository.delete(jpaType(operationEntity)); return null;});
  }

  @Override
  public Mono<ServiceInstanceOperationEntity> readByOperationId(String operationId) {
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
