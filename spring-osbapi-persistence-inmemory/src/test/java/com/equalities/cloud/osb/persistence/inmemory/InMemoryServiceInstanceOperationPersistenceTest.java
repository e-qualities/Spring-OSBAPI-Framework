package com.equalities.cloud.osb.persistence.inmemory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.equalities.cloud.osb.persistence.ServiceInstanceOperationEntity;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus;
import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;
import com.equalities.cloud.osb.persistence.inmemory.InMemoryServiceInstanceOperationPersistence;

@ExtendWith(MockitoExtension.class)
class InMemoryServiceInstanceOperationPersistenceTest {
  
  static final Duration duration = Duration.ofSeconds(3);
  
  private Map<String, ServiceInstanceOperationEntity> memory;
  private InMemoryServiceInstanceOperationPersistence instance;
  
  @Mock
  ServiceInstanceOperationEntity entity;
  
  @Mock
  ServiceOperationStatus status;

  @BeforeEach
  void setUp() throws Exception {
    memory = new HashMap<>();
    instance = new InMemoryServiceInstanceOperationPersistence(memory);
  }

  @Test
  void testInMemoryServiceInstanceOperationPersistence() {
    new InMemoryServiceInstanceOperationPersistence();
  }

  @Test
  void testInMemoryServiceInstanceOperationPersistenceDuration() {
    new InMemoryServiceInstanceOperationPersistence(Duration.ofMillis(20));
  }

  @Test
  void testInMemoryServiceInstanceOperationPersistenceMapOfStringServiceInstanceOperationEntity() {
    new InMemoryServiceInstanceOperationPersistence(memory);
  }

  @Test
  void testInsert() {
    String id = "someId";
    when(entity.getId()).thenReturn(id);
    instance.update(entity).block();
    assertThat(memory).containsEntry(entity.getId(), entity);
  }

  @Test
  void testUpdate() {
    String id = "someId";
    when(entity.getId()).thenReturn(id);
    instance.update(entity).block();
    assertThat(memory).containsEntry(entity.getId(), entity);
  }

  @Test
  void testDelete() {
    String id = "someId";
    when(entity.getId()).thenReturn(id);
    instance.insert(entity).block();
    assertThat(memory).containsEntry(entity.getId(), entity);
    instance.delete(entity).block();
    assertThat(memory).doesNotContainEntry(entity.getId(), entity);
  }

  @Test
  void testReadByOperationId() {
    String id = "someId";
    when(entity.getId()).thenReturn(id);
    instance.insert(entity).block();
    assertThat(memory).containsEntry(entity.getId(), entity);
    ServiceInstanceOperationEntity readEntity = instance.readByOperationId(id).block();
    assertThat(readEntity).isSameAs(entity);
  }
  
  @Test
  void testReadByOperationIdReturnsEmptyIfOperationNotFound() {
    String id = "someId";
    when(entity.getId()).thenReturn(id);
    assertThat(memory).doesNotContainEntry(entity.getId(), entity);
    ServiceInstanceOperationEntity readEntity = instance.readByOperationId(id).block();
    assertThat(readEntity).isNull();
  }
  
  @Test
  void testDeleteOperationsByStateOlderThanDuration() {
    memory.put("id", entity);
    when(entity.getCreatedAt()).thenReturn(Instant.now().minus(Duration.ofHours(5)));
    instance.deleteOperationsOlderThan(duration).block();
    
    verify(entity).getCreatedAt();
    assertThat(memory).isEmpty();
  }
  
  @ParameterizedTest
  @EnumSource
  void testDeleteOperationsByStateOlderThanStateDuration(State state) {
    memory.put("id", entity);
    when(entity.getCreatedAt()).thenReturn(Instant.now().minus(Duration.ofHours(5)));
    when(entity.getStatus()).thenReturn(status);
    when(status.getState()).thenReturn(state);
    
    instance.deleteOperationsByStateOlderThan(state, duration).block();
    
    verify(entity).getCreatedAt();
    verify(entity).getStatus();
    verify(status).getState();
    
    assertThat(memory).isEmpty();
  }
  
  @Test
  void testDeleteOperationsByStateOlderThanStateDurationDoesNotDeleteWrongState() {
    memory.put("id", entity);
    when(entity.getStatus()).thenReturn(status);
    when(status.getState()).thenReturn(State.IN_PROGRESS);
    
    instance.deleteOperationsByStateOlderThan(State.FAILED, duration).block();
    
    verify(entity).getStatus();
    verify(status).getState();
    
    assertThat(memory).isNotEmpty();
  }
}
