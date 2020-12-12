package com.equalities.cloud.osb.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.equalities.cloud.osb.config.OsbApiConfig;
import com.equalities.cloud.osb.persistence.ScheduledOperationRemovalTask;
import com.equalities.cloud.osb.persistence.ScheduledPersistenceTasksConfiguration;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;

@ExtendWith(MockitoExtension.class)
class ScheduledPersistenceTasksConfigurationTest {
  //create an ApplicationContextRunner that will 
  // create a context with the configuration under test.
  ApplicationContextRunner contextRunner;
  
  @Mock
  ServiceInstanceOperationPersistence instanceOperationPersistence;
  
  @Mock
  ServiceInstanceBindingOperationPersistence bindingOperationPersistence;
  
  @Mock
  OsbApiConfig config;
  
  @BeforeEach
  void setUp() {
    contextRunner = new ApplicationContextRunner()
                          .withBean(ServiceInstanceOperationPersistence.class, () -> {return instanceOperationPersistence;})
                          .withBean(ServiceInstanceBindingOperationPersistence.class, () -> {return bindingOperationPersistence;})
                          .withBean(OsbApiConfig.class, () -> {return config;})
                          .withConfiguration(AutoConfigurations.of(ScheduledPersistenceTasksConfiguration.class));
  }

  @Test
  @DisplayName("exposes all necessary beans.")
  void testAllBeansExposed() {
    contextRunner
    .run((context) -> {
      assertThat(context).getBean(ScheduledOperationRemovalTask.class).isNotNull();
   });
  }
}

