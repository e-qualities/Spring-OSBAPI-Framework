package com.equalities.cloud.osb.persistence.inmemory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.equalities.cloud.osb.persistence.ServiceInstanceBindingEntity;
import com.equalities.cloud.osb.persistence.ServiceInstanceEntity;
import com.equalities.cloud.osb.persistence.inmemory.InMemoryServiceInstancePersistence;

@ExtendWith(MockitoExtension.class)
class InMemoryServiceInstancePersistenceTest {
  
  private Map<String, ServiceInstanceEntity> memory;
  private InMemoryServiceInstancePersistence instance;
  
  @Mock
  ServiceInstanceEntity entity;

  @BeforeEach
  void setUp() throws Exception {
    memory = new HashMap<>();
    instance = new InMemoryServiceInstancePersistence(memory);
  }

  @Test
  void testInMemoryServiceInstancePersistence() {
    new InMemoryServiceInstancePersistence();
  }

  @Test
  void testInMemoryServiceInstancePersistenceDuration() {
    new InMemoryServiceInstancePersistence(Duration.ofMillis(20));
  }

  @Test
  void testInMemoryServiceInstancePersistenceMapOfStringServiceInstanceEntity() {
    new InMemoryServiceInstancePersistence(memory);
  }

  @Test
  void testInsert() {
    String id = "someId";
    when(entity.getServiceInstanceId()).thenReturn(id);
    instance.update(entity).block();
    assertThat(memory).containsEntry(entity.getServiceInstanceId(), entity);
  }

  @Test
  void testUpdate() {
    String id = "someId";
    when(entity.getServiceInstanceId()).thenReturn(id);
    instance.update(entity).block();
    assertThat(memory).containsEntry(entity.getServiceInstanceId(), entity);
  }

  @Test
  void testDelete() {
    String id = "someId";
    when(entity.getServiceInstanceId()).thenReturn(id);
    instance.insert(entity).block();
    assertThat(memory).containsEntry(entity.getServiceInstanceId(), entity);
    instance.delete(entity).block();
    assertThat(memory).doesNotContainEntry(entity.getServiceInstanceId(), entity);
  }

  @Test
  void testReadByServiceInstanceId() {
    String id = "someId";
    when(entity.getServiceInstanceId()).thenReturn(id);
    instance.insert(entity).block();
    assertThat(memory).containsEntry(entity.getServiceInstanceId(), entity);
    ServiceInstanceEntity readEntity = instance.readByServiceInstanceId(id).block();
    assertThat(readEntity).isSameAs(entity);
  }
  
  @Test
  void testReadByServiceInstanceIdReturnsEmptyIfOperationNotFound() {
    String id = "someId";
    when(entity.getServiceInstanceId()).thenReturn(id);
    assertThat(memory).doesNotContainEntry(entity.getServiceInstanceId(), entity);
    ServiceInstanceEntity readEntity = instance.readByServiceInstanceId(id).block();
    assertThat(readEntity).isNull();
  }

}
