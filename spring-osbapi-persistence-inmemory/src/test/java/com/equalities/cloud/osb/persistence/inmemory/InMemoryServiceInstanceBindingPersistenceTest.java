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
import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.inmemory.InMemoryServiceInstanceBindingPersistence;

@ExtendWith(MockitoExtension.class)
class InMemoryServiceInstanceBindingPersistenceTest {
  
  private Map<String, ServiceInstanceBindingEntity> memory;
  private InMemoryServiceInstanceBindingPersistence instance;
  
  @Mock
  ServiceInstanceBindingEntity entity;

  @BeforeEach
  void setUp() throws Exception {
    memory = new HashMap<>();
    instance = new InMemoryServiceInstanceBindingPersistence(memory);
  }

  @Test
  void testInMemoryServiceInstanceBindingPersistence() {
    new InMemoryServiceInstanceBindingPersistence();
  }

  @Test
  void testInMemoryServiceInstanceBindingPersistenceDuration() {
    new InMemoryServiceInstanceBindingPersistence(Duration.ofMillis(20));
  }

  @Test
  void testInMemoryServiceInstanceBindingPersistenceMapOfStringServiceInstanceBindingEntity() {
    new InMemoryServiceInstanceBindingPersistence(memory);
  }

  @Test
  void testUpdate() {
    String id = "someId";
    when(entity.getServiceInstanceBindingId()).thenReturn(id);
    instance.update(entity).block();
    assertThat(memory).containsEntry(entity.getServiceInstanceBindingId(), entity);
  }

  @Test
  void testDelete() {
    String id = "someId";
    when(entity.getServiceInstanceBindingId()).thenReturn(id);
    instance.insert(entity).block();
    assertThat(memory).containsEntry(entity.getServiceInstanceBindingId(), entity);
    instance.delete(entity).block();
    assertThat(memory).doesNotContainEntry(entity.getServiceInstanceBindingId(), entity);
  }

  @Test
  void testReadByServiceInstanceBindingId() {
    String id = "someId";
    when(entity.getServiceInstanceBindingId()).thenReturn(id);
    instance.insert(entity).block();
    assertThat(memory).containsEntry(entity.getServiceInstanceBindingId(), entity);
    ServiceInstanceBindingEntity readEntity = instance.readByServiceInstanceBindingId(id).block();
    assertThat(readEntity).isSameAs(entity);
  }
  
  @Test
  void testReadByServiceInstanceBindingIdReturnsEmptyIfOperationNotFound() {
    String id = "someId";
    when(entity.getServiceInstanceBindingId()).thenReturn(id);
    assertThat(memory).doesNotContainEntry(entity.getServiceInstanceBindingId(), entity);
    ServiceInstanceBindingEntity readEntity = instance.readByServiceInstanceBindingId(id).block();
    assertThat(readEntity).isNull();
  }
}
