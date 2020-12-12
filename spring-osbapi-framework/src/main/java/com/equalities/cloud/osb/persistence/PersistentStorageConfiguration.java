package com.equalities.cloud.osb.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;

@Configuration
public class PersistentStorageConfiguration {

  @Bean
  public PersistentStorage persistentStorage(ServiceInstancePersistence instancePersistence, 
                                             ServiceInstanceOperationPersistence instanceOperationPersistence,
                                             ServiceInstanceBindingPersistence bindingPersistence,
                                             ServiceInstanceBindingOperationPersistence bindingOperationPersistence) {
    return new PersistentStorage(instancePersistence, instanceOperationPersistence, bindingPersistence, bindingOperationPersistence);
  }
}
