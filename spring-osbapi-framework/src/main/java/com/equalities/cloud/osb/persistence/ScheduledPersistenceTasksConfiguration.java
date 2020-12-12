package com.equalities.cloud.osb.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.equalities.cloud.osb.config.OsbApiConfig;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;

@Configuration
@EnableScheduling
public class ScheduledPersistenceTasksConfiguration {

  @Bean
  public ScheduledOperationRemovalTask scheduledOperationsRemovalTask(ServiceInstanceOperationPersistence instanceOperationPersistence,
                                                                      ServiceInstanceBindingOperationPersistence bindingOperationPersistence,
                                                                      OsbApiConfig configs) {
    return new ScheduledOperationRemovalTask(instanceOperationPersistence, bindingOperationPersistence, configs);
  }
}
