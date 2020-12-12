package com.equalities.cloud.osb.persistence.mongodb;

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
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.ServiceInstancePersistence;
import com.equalities.cloud.osb.persistence.mongodb.MongoDbPersistenceConfiguration;
import com.equalities.cloud.osb.persistence.mongodb.MongoDbServiceInstanceBindingOperationPersistence;
import com.equalities.cloud.osb.persistence.mongodb.MongoDbServiceInstanceBindingPersistence;
import com.equalities.cloud.osb.persistence.mongodb.MongoDbServiceInstanceOperationPersistence;
import com.equalities.cloud.osb.persistence.mongodb.MongoDbServiceInstancePersistence;
import com.mongodb.reactivestreams.client.MongoClient;

@DisplayName("MongoDbPersistenceConfiguration")
@ExtendWith(MockitoExtension.class)
class MongoDbPersistenceConfigurationTest {
  //create an ApplicationContextRunner that will 
  // create a context with the configuration under test.
  ApplicationContextRunner contextRunner;
  
  @Mock
  static MongoDbServiceInstanceOperationPersistence instanceOperationPersistenceMock;
  
  @Mock
  static MongoDbServiceInstancePersistence instancePersistenceMock; 
  
  @Mock
  static MongoDbServiceInstanceBindingOperationPersistence instanceBindingOperationPersistenceMock;
  
  @Mock
  static MongoDbServiceInstanceBindingPersistence instanceBindingPersistenceMock;
  
  @Mock
  MongoClient mongoClient;
  
  @BeforeEach
  void setUp() {
    contextRunner = new ApplicationContextRunner()
                        .withConfiguration(AutoConfigurations.of(MongoDbPersistenceConfiguration.class));
  }
  
  @Test
  @DisplayName("exposes all necessary beans.")
  void testAllBeansExposed() {
    contextRunner
    .withBean(ReactiveMongoTemplate.class, mongoClient, "databaseName")
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
