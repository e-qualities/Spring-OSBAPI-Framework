package com.equalities.cloud.osb.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import com.equalities.cloud.osb.persistence.PersistentStorage;
import com.equalities.cloud.osb.persistence.PersistentStorageConfiguration;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;

@ExtendWith(MockitoExtension.class)
class PersistentStorageConfigurationTest {
  //create an ApplicationContextRunner that will 
  // create a context with the configuration under test.
  ApplicationContextRunner contextRunner;
  
  @Mock
  static PersistentStorage customStorage;
  
  @Mock
  ServiceInstancePersistence instancePersistence;
  
  @Mock
  ServiceInstanceOperationPersistence instanceOperationPersistence;
  
  @Mock
  ServiceInstanceBindingPersistence bindingPersistence;
  
  @Mock
  ServiceInstanceBindingOperationPersistence bindingOperationPersistence;
  
  @BeforeEach
  void setUp() {
    contextRunner = new ApplicationContextRunner()
                          .withBean(ServiceInstancePersistence.class, () -> {return instancePersistence;})
                          .withBean(ServiceInstanceOperationPersistence.class, () -> {return instanceOperationPersistence;})
                          .withBean(ServiceInstanceBindingPersistence.class, () -> {return bindingPersistence;})
                          .withBean(ServiceInstanceBindingOperationPersistence.class, () -> {return bindingOperationPersistence;})
                          .withConfiguration(AutoConfigurations.of(PersistentStorageConfiguration.class));
  }

  @Test
  @DisplayName("exposes all necessary beans.")
  void testAllBeansExposed() {
    contextRunner
    .run((context) -> {
      assertThat(context).getBean(PersistentStorage.class).isNotNull();
   });
  }
  
  @Test
  @DisplayName("does NOT allow overriding of declared beans by application.")
  void testCustomBeanOverrides() {
    
    assertThatThrownBy(() -> {
      contextRunner
      .withUserConfiguration(CustomApplicationConfiguration.class)
      .run((context) -> {
        context.getBean(PersistentStorage.class);
      });
    }).isInstanceOf(NoUniqueBeanDefinitionException.class);
  }
  
  static class CustomApplicationConfiguration {
    @Bean
    public PersistentStorage customStorage() {
      return customStorage;
    }
  }
}
