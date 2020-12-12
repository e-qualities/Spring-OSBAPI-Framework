package com.equalities.cloud.osb.persistence.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;

@Configuration
public class JPAPersistenceConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ServiceInstanceOperationPersistence serviceInstanceOperationPersistence(ServiceInstanceOperationRepository repository) {
    return new JPAServiceInstanceOperationPersistence(repository);
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceInstancePersistence serviceInstancePersistence(ServiceInstanceRepository repository) {
    return new JPAServiceInstancePersistence(repository);
  }
  
  @Bean
  @ConditionalOnMissingBean
  public ServiceInstanceBindingOperationPersistence serviceInstanceBindingOperationPersistence(ServiceInstanceBindingOperationRepository repository) {
    return new JPAServiceInstanceBindingOperationPersistence(repository);
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceInstanceBindingPersistence serviceInstanceBindingPersistence(ServiceInstanceBindingRepository repository) {
    return new JPAServiceInstanceBindingPersistence(repository);
  }
}
