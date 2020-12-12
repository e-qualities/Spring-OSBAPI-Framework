package com.equalities.cloud.osb.autoconfiguration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import com.equalities.cloud.osb.autoconfiguration.JPAPersistenceAutoConfiguration;
import com.equalities.cloud.osb.autoconfiguration.OsbApiConfigAutoConfiguration;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.jpa.JPAServiceInstancePersistence;

@ExtendWith(MockitoExtension.class)
class JPAPersistenceAutoConfigurationTest {
  
  @DataJpaTest
  @ContextConfiguration(classes = {JPAPersistenceAutoConfiguration.class, OsbApiConfigAutoConfiguration.class})
  static class BeanExpositionTest {
    @Autowired
    ApplicationContext context;
    
    @Test
    @DisplayName("exposes all necessary beans.")
    void testAllBeansExposed() {
      assertThat(context.getBean(ServiceInstanceOperationPersistence.class)).isNotNull();
      assertThat(context.getBean(ServiceInstancePersistence.class)).isNotNull();
      assertThat(context.getBean(ServiceInstanceBindingOperationPersistence.class)).isNotNull();
      assertThat(context.getBean(ServiceInstanceBindingPersistence.class)).isNotNull();
    }
  }
  
  @DataJpaTest
  @ContextConfiguration(classes = {CustomBeanExpositionTest.CustomApplicationConfiguration.class, JPAPersistenceAutoConfiguration.class, OsbApiConfigAutoConfiguration.class})
  static class CustomBeanExpositionTest {
    @Autowired
    ApplicationContext context;
    
    @Autowired
    CustomApplicationConfiguration customConfig;
    
    @Test
    @DisplayName("exposes all necessary beans.")
    void testCustomBeanOverrides() {
      assertThat(context.getBean(ServiceInstanceOperationPersistence.class)).isNotNull();
      assertThat(context.getBean(ServiceInstancePersistence.class)).isNotNull();
      assertThat(context.getBean(ServiceInstanceBindingOperationPersistence.class)).isNotNull();
      assertThat(context.getBean(ServiceInstanceBindingPersistence.class)).isNotNull();
      
      assertThat(context.getBean(ServiceInstanceOperationPersistence.class)).isEqualTo(customConfig.serviceInstanceOperationPersistence());
      assertThat(context.getBean(ServiceInstancePersistence.class)).isEqualTo(customConfig.serviceInstancePersistence());
      assertThat(context.getBean(ServiceInstanceBindingOperationPersistence.class)).isEqualTo(customConfig.serviceInstanceBindingOperationPersistence());
      assertThat(context.getBean(ServiceInstanceBindingPersistence.class)).isEqualTo(customConfig.serviceInstanceBindingPersistence());
    }
    
    static class CustomApplicationConfiguration {
      
      static JPAServiceInstanceOperationPersistence instanceOperationPersistenceMock = Mockito.mock(JPAServiceInstanceOperationPersistence.class);
      
      static JPAServiceInstancePersistence instancePersistenceMock = Mockito.mock(JPAServiceInstancePersistence.class);
      
      static JPAServiceInstanceBindingOperationPersistence instanceBindingOperationPersistenceMock = Mockito.mock(JPAServiceInstanceBindingOperationPersistence.class);

      static JPAServiceInstanceBindingPersistence instanceBindingPersistenceMock = Mockito.mock(JPAServiceInstanceBindingPersistence.class);
      
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
}
