package com.equalities.cloud.osb.persistence.mongodb;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;

import reactor.core.publisher.Mono;

public class MongoDbServiceInstancePersistence implements ServiceInstancePersistence {

  private ReactiveMongoTemplate reactiveMongoTemplate;

  public MongoDbServiceInstancePersistence(ReactiveMongoTemplate reactiveMongoTemplate) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
  }

  @Override
  public Mono<ServiceInstanceEntity> insert(ServiceInstanceEntity entity) {
    return reactiveMongoTemplate.insert(entity);
  }
  
  @Override
  public Mono<ServiceInstanceEntity> update(ServiceInstanceEntity entity) {
    return reactiveMongoTemplate.save(entity);
  }

  @Override
  public Mono<Void> delete(ServiceInstanceEntity entity) {
    return reactiveMongoTemplate.remove(entity).then();
  }

  @Override
  public Mono<ServiceInstanceEntity> readByServiceInstanceId(String serviceInstanceId) {
    return reactiveMongoTemplate.findById(serviceInstanceId, ServiceInstanceEntity.class);
  }
}
