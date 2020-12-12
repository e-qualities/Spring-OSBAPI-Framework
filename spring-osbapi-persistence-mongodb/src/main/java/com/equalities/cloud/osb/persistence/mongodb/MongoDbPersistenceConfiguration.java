package com.equalities.cloud.osb.persistence.mongodb;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;

@Configuration
public class MongoDbPersistenceConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ServiceInstanceOperationPersistence serviceInstanceOperationPersistence(ReactiveMongoTemplate reactiveMongoTemplate) {
    return new MongoDbServiceInstanceOperationPersistence(reactiveMongoTemplate);
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceInstancePersistence serviceInstancePersistence(ReactiveMongoTemplate reactiveMongoTemplate) {
    return new MongoDbServiceInstancePersistence(reactiveMongoTemplate);
  }
  
  @Bean
  @ConditionalOnMissingBean
  public ServiceInstanceBindingOperationPersistence serviceInstanceBindingOperationPersistence(ReactiveMongoTemplate reactiveMongoTemplate) {
    return new MongoDbServiceInstanceBindingOperationPersistence(reactiveMongoTemplate);
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceInstanceBindingPersistence serviceInstanceBindingPersistence(ReactiveMongoTemplate reactiveMongoTemplate) {
    return new MongoDbServiceInstanceBindingPersistence(reactiveMongoTemplate);
  }
}
