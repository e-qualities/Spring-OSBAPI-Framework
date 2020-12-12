package com.equalities.cloud.osb.persistence.inmemory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;

@Configuration
public class InMemoryPersistenceConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ServiceInstanceOperationPersistence serviceInstanceOperationPersistence() {
    return new InMemoryServiceInstanceOperationPersistence();
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceInstancePersistence serviceInstancePersistence() {
    return new InMemoryServiceInstancePersistence();
  }
  
  @Bean
  @ConditionalOnMissingBean
  public ServiceInstanceBindingOperationPersistence serviceInstanceBindingOperationPersistence() {
    return new InMemoryServiceInstanceBindingOperationPersistence();
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceInstanceBindingPersistence serviceInstanceBindingPersistence() {
    return new InMemoryServiceInstanceBindingPersistence();
  }
}
