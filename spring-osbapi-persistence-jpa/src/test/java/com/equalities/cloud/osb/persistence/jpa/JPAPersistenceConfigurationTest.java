package com.equalities.cloud.osb.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;
import com.equalities.cloud.osb.persistence.jpa.JPAPersistenceConfiguration;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstancePersistence;
import com.equalities.cloud.osb.persistence.jpa.ServiceInstanceBindingOperationRepository;
import com.equalities.cloud.osb.persistence.jpa.ServiceInstanceBindingRepository;
import com.equalities.cloud.osb.persistence.jpa.ServiceInstanceOperationRepository;
import com.equalities.cloud.osb.persistence.jpa.ServiceInstanceRepository;

@ExtendWith(MockitoExtension.class)
class JPAPersistenceConfigurationTest {
  //create an ApplicationContextRunner that will 
  // create a context with the configuration under test.
  ApplicationContextRunner contextRunner;
  
  @Mock
  static JPAServiceInstanceOperationPersistence instanceOperationPersistenceMock;
  
  @Mock
  static JPAServiceInstancePersistence instancePersistenceMock; 
  
  @Mock
  static JPAServiceInstanceBindingOperationPersistence instanceBindingOperationPersistenceMock;
  
  @Mock
  static JPAServiceInstanceBindingPersistence instanceBindingPersistenceMock;
  
  @Mock
  ServiceInstanceOperationRepository serviceInstanceOperationRepository;
  
  @Mock
  ServiceInstanceRepository serviceInstanceRepository;
  
  @Mock
  ServiceInstanceBindingOperationRepository serviceInstanceBindingOperationRepository;
  
  @Mock
  ServiceInstanceBindingRepository serviceInstanceBindingRepository;
  
  @BeforeEach
  void setUp() {
    contextRunner = new ApplicationContextRunner()
                        .withConfiguration(AutoConfigurations.of(JPAPersistenceConfiguration.class));
  }
  
  @Test
  @DisplayName("exposes all necessary beans.")
  void testAllBeansExposed() {
    contextRunner
    .withBean(ServiceInstanceOperationRepository.class,        () -> serviceInstanceOperationRepository)
    .withBean(ServiceInstanceRepository.class,                 () -> serviceInstanceRepository)
    .withBean(ServiceInstanceBindingOperationRepository.class, () -> serviceInstanceBindingOperationRepository)
    .withBean(ServiceInstanceBindingRepository.class,          () -> serviceInstanceBindingRepository)
    
    .run((context) -> {
      assertThat(context).getBean(ServiceInstanceOperationPersistence.class).isNotNull();
      assertThat(context).getBean(ServiceInstancePersistence.class).isNotNull();
      assertThat(context).getBean(ServiceInstanceBindingOperationPersistence.class).isNotNull();
      assertThat(context).getBean(ServiceInstanceBindingPersistence.class).isNotNull();
   });
  }
  
  @Test
  @DisplayName("allows overriding of declared beans by application.")
  void testCustomBeanOverrides() {

    
    contextRunner
    .withUserConfiguration(CustomApplicationConfiguration.class)
    .run((context) -> {
      assertThat(context).getBean(ServiceInstanceOperationPersistence.class).isNotNull();
      assertThat(context).getBean(ServiceInstancePersistence.class).isNotNull();
      assertThat(context).getBean(ServiceInstanceBindingOperationPersistence.class).isNotNull();
      assertThat(context).getBean(ServiceInstanceBindingPersistence.class).isNotNull();
      
      assertThat(context).getBean(ServiceInstanceOperationPersistence.class).isSameAs(instanceOperationPersistenceMock);
      assertThat(context).getBean(ServiceInstancePersistence.class).isSameAs(instancePersistenceMock);
      assertThat(context).getBean(ServiceInstanceBindingOperationPersistence.class).isSameAs(instanceBindingOperationPersistenceMock);
      assertThat(context).getBean(ServiceInstanceBindingPersistence.class).isSameAs(instanceBindingPersistenceMock);
   });
  }

  static class CustomApplicationConfiguration {
    @Bean
    public ServiceInstanceOperationPersistence serviceInstanceOperationPersistence() {
      return instanceOperationPersistenceMock;
    }

    @Bean
    public ServiceInstancePersistence serviceInstancePersistence() {
      return instancePersistenceMock;
    }
    
    @Bean
    public ServiceInstanceBindingOperationPersistence serviceInstanceBindingOperationPersistence() {
      return instanceBindingOperationPersistenceMock;
    }

    @Bean
    public ServiceInstanceBindingPersistence serviceInstanceBindingPersistence() {
      return instanceBindingPersistenceMock;
    }
  }
}
