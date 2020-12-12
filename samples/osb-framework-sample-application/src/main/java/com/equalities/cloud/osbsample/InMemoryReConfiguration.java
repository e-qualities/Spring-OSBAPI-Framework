package com.equalities.cloud.osbsample;
//package com.equalities.cloud.osbsample;
//
//import java.time.Duration;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//
//import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
//import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
//import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
//import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;
//import com.equalities.cloud.osb.persistence.inmemory.InMemoryServiceInstanceBindingOperationPersistence;
//import com.equalities.cloud.osb.persistence.inmemory.InMemoryServiceInstanceBindingPersistence;
//import com.equalities.cloud.osb.persistence.inmemory.InMemoryServiceInstanceOperationPersistence;
//import com.equalities.cloud.osb.persistence.inmemory.InMemoryServiceInstancePersistence;
//
///**
// * Sample configuration that shows how an application can still re-configure the persistence configurations.
// * Note, that an application does not HAVE to do it. Just remove the configuration here to use the meaningful defaults.
// */
//@Configuration
//@Profile("inmemory")
//public class InMemoryReConfiguration {
//  
//  private static final Duration delay = Duration.ofSeconds(4);
//  
//  @Bean
//  public ServiceInstanceOperationPersistence serviceInstanceOperationPersistence() {
//    return new InMemoryServiceInstanceOperationPersistence(delay);
//  }
//
//  @Bean
//  public ServiceInstancePersistence serviceInstancePersistence() {
//    return new InMemoryServiceInstancePersistence(delay);
//  }
//  
//  @Bean
//  public ServiceInstanceBindingOperationPersistence serviceInstanceBindingOperationPersistence() {
//    return new InMemoryServiceInstanceBindingOperationPersistence(delay);
//  }
//
//  @Bean
//  public ServiceInstanceBindingPersistence serviceInstanceBindingPersistence() {
//    return new InMemoryServiceInstanceBindingPersistence(delay);
//  }
//}
