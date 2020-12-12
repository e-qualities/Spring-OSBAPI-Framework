package com.equalities.cloud.osb.persistence.mongodb;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;

import reactor.core.publisher.Mono;

public class MongoDbServiceInstanceBindingPersistence implements ServiceInstanceBindingPersistence {

  private ReactiveMongoTemplate reactiveMongoTemplate;

  public MongoDbServiceInstanceBindingPersistence(ReactiveMongoTemplate reactiveMongoTemplate) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
  }

  @Override
  public Mono<ServiceInstanceBindingEntity> insert(ServiceInstanceBindingEntity entity) {
    return reactiveMongoTemplate.insert(entity);
  }
  
  @Override
  public Mono<ServiceInstanceBindingEntity> update(ServiceInstanceBindingEntity entity) {
    return reactiveMongoTemplate.save(entity);
  }

  @Override
  public Mono<Void> delete(ServiceInstanceBindingEntity entity) {
    return reactiveMongoTemplate.remove(entity).then();
  }

  @Override
  public Mono<ServiceInstanceBindingEntity> readByServiceInstanceBindingId(String serviceInstanceId) {
    return reactiveMongoTemplate.findById(serviceInstanceId, ServiceInstanceBindingEntity.class);
  }
}
