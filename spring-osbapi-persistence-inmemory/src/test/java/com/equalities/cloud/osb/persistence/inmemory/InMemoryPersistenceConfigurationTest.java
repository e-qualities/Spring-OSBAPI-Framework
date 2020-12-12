package com.equalities.cloud.osb.persistence.inmemory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;
import com.equalities.cloud.osb.persistence.inmemory.InMemoryPersistenceConfiguration;
import com.equalities.cloud.osb.persistence.inmemory.InMemoryServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.inmemory.InMemoryServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.inmemory.InMemoryServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.inmemory.InMemoryServiceInstancePersistence;

@DisplayName("InMemoryPersistenceConfiguration")
@ExtendWith(MockitoExtension.class)
class InMemoryPersistenceConfigurationTest {
  //create an ApplicationContextRunner that will 
  // create a context with the configuration under test.
  ApplicationContextRunner contextRunner;
  
  @Mock
  static InMemoryServiceInstanceOperationPersistence instanceOperationPersistenceMock;
  
  @Mock
  static InMemoryServiceInstancePersistence instancePersistenceMock; 
  
  @Mock
  static InMemoryServiceInstanceBindingOperationPersistence instanceBindingOperationPersistenceMock;
  
  @Mock
  static InMemoryServiceInstanceBindingPersistence instanceBindingPersistenceMock;
  
  @BeforeEach
  void setUp() {
    contextRunner = new ApplicationContextRunner()
                          .withConfiguration(AutoConfigurations.of(InMemoryPersistenceConfiguration.class));
  }
  
  @Test
  @DisplayName("exposes all necessary beans.")
  void testAllBeansExposed() {
    contextRunner
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
