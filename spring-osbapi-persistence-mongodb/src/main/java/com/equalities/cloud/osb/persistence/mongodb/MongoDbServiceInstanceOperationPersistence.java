package com.equalities.cloud.osb.persistence.mongodb;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;

import reactor.core.publisher.Mono;

public class MongoDbServiceInstanceOperationPersistence implements ServiceInstanceOperationPersistence {

  private ReactiveMongoTemplate reactiveMongoTemplate;

  public MongoDbServiceInstanceOperationPersistence(ReactiveMongoTemplate reactiveMongoTemplate) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
  }

  @Override
  public Mono<ServiceInstanceOperationEntity> insert(ServiceInstanceOperationEntity operationEntity) {
    return reactiveMongoTemplate.insert(operationEntity);
  }

  @Override
  public Mono<ServiceInstanceOperationEntity> update(ServiceInstanceOperationEntity operationEntity) {
    return reactiveMongoTemplate.save(operationEntity);
  }

  @Override
  public Mono<Void> delete(ServiceInstanceOperationEntity operationEntity) {
    return reactiveMongoTemplate.remove(operationEntity).then();
  }

  @Override
  public Mono<ServiceInstanceOperationEntity> readByOperationId(String operationId) {
    return reactiveMongoTemplate.findById(operationId, ServiceInstanceOperationEntity.class);
  }

  @Override
  public Mono<Void> deleteOperationsOlderThan(Duration duration) {
    Query query = new Query(Criteria.where("createdAt").lt(Instant.now().minus(duration)));
    return reactiveMongoTemplate.remove(query, ServiceInstanceOperationEntity.class).then();
  }

  @Override
  public Mono<Void> deleteOperationsByStateOlderThan(ServiceOperationStatus.State state, Duration duration) {
    Query query = new Query(Criteria.where("status.state").is(state).and("createdAt").lt(Instant.now().minus(duration)));
    return reactiveMongoTemplate.remove(query, ServiceInstanceOperationEntity.class).then();
  }
}
