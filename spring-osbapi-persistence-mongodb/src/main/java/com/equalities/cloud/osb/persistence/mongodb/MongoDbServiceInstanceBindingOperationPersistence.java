package com.equalities.cloud.osb.persistence.mongodb;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;

import reactor.core.publisher.Mono;

public class MongoDbServiceInstanceBindingOperationPersistence implements ServiceInstanceBindingOperationPersistence {

  private ReactiveMongoTemplate reactiveMongoTemplate;

  public MongoDbServiceInstanceBindingOperationPersistence(ReactiveMongoTemplate reactiveMongoTemplate) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
  }

  @Override
  public Mono<ServiceInstanceBindingOperationEntity> insert(ServiceInstanceBindingOperationEntity operationEntity) {
    return reactiveMongoTemplate.insert(operationEntity);
  }

  @Override
  public Mono<ServiceInstanceBindingOperationEntity> update(ServiceInstanceBindingOperationEntity operationEntity) {
    return reactiveMongoTemplate.save(operationEntity);
  }

  @Override
  public Mono<Void> delete(ServiceInstanceBindingOperationEntity operationEntity) {
    return reactiveMongoTemplate.remove(operationEntity).then();
  }

  @Override
  public Mono<ServiceInstanceBindingOperationEntity> readByOperationId(String operationId) {
    return reactiveMongoTemplate.findById(operationId, ServiceInstanceBindingOperationEntity.class);
  }

  @Override
  public Mono<Void> deleteOperationsOlderThan(Duration duration) {
    Query query = new Query(Criteria.where("createdAt").lt(Instant.now().minus(duration)));
    return reactiveMongoTemplate.remove(query, ServiceInstanceBindingOperationEntity.class).then();
  }

  @Override
  public Mono<Void> deleteOperationsByStateOlderThan(ServiceOperationStatus.State state, Duration duration) {
    Query query = new Query(Criteria.where("status.state").is(state).and("createdAt").lt(Instant.now().minus(duration)));
    return reactiveMongoTemplate.remove(query, ServiceInstanceBindingOperationEntity.class).then();
  }
}
